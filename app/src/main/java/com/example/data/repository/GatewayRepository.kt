package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.model.ApiKeyItem
import com.example.model.BusinessAccount
import com.example.model.GatewayDispatch
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class GatewayRepository(private val context: Context) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.getString(R.string.firestore_database_id)
    )
    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogle(
        activity: Activity,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.Main) {
        val serverClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Google Sign-In configuration missing")
            return@withContext
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        try {
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                authResult.user?.let { onSuccess(it) } ?: onError("User profile not found")
            } else {
                onError("Unsupported credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("PulseGateway", "Google Sign-In dismissed: ${e.message}")
        } catch (e: Exception) {
            Log.e("PulseGateway", "Google Sign-In failed", e)
            onError(e.localizedMessage ?: "Sign-in failed")
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        auth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("PulseGateway", "Clear credential failed", e)
        }
    }

    suspend fun convertToBusinessAccount(orgName: String): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        val email = auth.currentUser?.email ?: ""
        return@withContext try {
            val businessDoc = db.collection("businesses").document(uid)
            val payload = hashMapOf(
                "userId" to uid,
                "organizationName" to orgName.trim(),
                "contactEmail" to email,
                "createdAt" to FieldValue.serverTimestamp(),
                "isVerified" to true
            )
            businessDoc.set(payload).await()

            // Automatically generate their first API Key
            generateApiKey(orgName.trim())
            true
        } catch (e: Exception) {
            Log.e("PulseGateway", "Failed to convert to business account", e)
            false
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeBusinessAccount(): Flow<BusinessAccount?> = observeAuthState().flatMapLatest { user ->
        if (user == null) {
            flowOf<BusinessAccount?>(null)
        } else {
            callbackFlow<BusinessAccount?> {
                val registration = db.collection("businesses").document(user.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("PulseGateway", "Error observing business account: ${error.message}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            trySend(snapshot.toObject(BusinessAccount::class.java))
                        } else {
                            trySend(null)
                        }
                    }

                awaitClose { registration.remove() }
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun generateApiKey(appName: String): String? = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext null
        return@withContext try {
            val randomToken = UUID.randomUUID().toString().replace("-", "").take(16)
            val formattedKey = "pulse_live_$randomToken"
            val keyDoc = db.collection("api_keys").document()

            val payload = hashMapOf(
                "keyId" to keyDoc.id,
                "userId" to uid,
                "appName" to appName.ifBlank { "My Business App" },
                "apiKey" to formattedKey,
                "createdAt" to FieldValue.serverTimestamp(),
                "isActive" to true,
                "totalDispatched" to 0L
            )
            keyDoc.set(payload).await()
            formattedKey
        } catch (e: Exception) {
            Log.e("PulseGateway", "Failed to generate API Key", e)
            null
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeApiKeys(): Flow<List<ApiKeyItem>> = observeAuthState().flatMapLatest { user ->
        if (user == null) {
            flowOf(emptyList())
        } else {
            callbackFlow {
                val registration = db.collection("api_keys")
                    .whereEqualTo("userId", user.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("PulseGateway", "Error observing API Keys: ${error.message}")
                            return@addSnapshotListener
                        }
                        val items = snapshot?.toObjects(ApiKeyItem::class.java) ?: emptyList()
                        trySend(items)
                    }

                awaitClose { registration.remove() }
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Real-time stream of all gateway dispatches.
     * Connected only when an authenticated session exists, preventing PERMISSION_DENIED errors.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeLiveDispatches(): Flow<List<GatewayDispatch>> = observeAuthState().flatMapLatest { user ->
        if (user == null) {
            flowOf(emptyList())
        } else {
            callbackFlow {
                val registration = db.collection("gateway_dispatches")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("PulseGateway", "Error observing dispatches: ${error.message}")
                            return@addSnapshotListener
                        }
                        val dispatches = snapshot?.toObjects(GatewayDispatch::class.java) ?: emptyList()
                        trySend(dispatches)

                        // Auto-acknowledge pending dispatches
                        snapshot?.documents?.forEach { doc ->
                            if (doc.getString("status") == "PENDING") {
                                val created = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                                val latency = System.currentTimeMillis() - created
                                doc.reference.update(
                                    mapOf(
                                        "status" to "DELIVERED",
                                        "deliveredAt" to FieldValue.serverTimestamp(),
                                        "latencyMs" to latency.coerceAtLeast(120L)
                                    )
                                )
                            }
                        }
                    }

                awaitClose { registration.remove() }
            }
        }
    }.flowOn(Dispatchers.IO)

    sealed class OtpVerificationResult {
        data class Success(val senderName: String, val otpCode: String, val latencyMs: Long) : OtpVerificationResult()
        object Expired : OtpVerificationResult()
        object InvalidOrNotFound : OtpVerificationResult()
        data class Error(val message: String) : OtpVerificationResult()
    }

    /**
     * Simulator: Directly triggers an external app dispatch through the gateway
     */
    suspend fun simulateExternalAppDispatch(
        appName: String,
        recipientPhone: String,
        otpCode: String,
        apiKey: String
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val docRef = db.collection("gateway_dispatches").document()
            val now = System.currentTimeMillis()
            val expiresAt = now + (5 * 60 * 1000L) // 5-minute validity window
            val payload = hashMapOf(
                "dispatchId" to docRef.id,
                "apiKey" to apiKey,
                "senderName" to appName.ifBlank { "Verified Gateway" },
                "recipientPhone" to recipientPhone,
                "otpCode" to otpCode,
                "body" to "$appName: Your verification code is $otpCode. Valid for 5 minutes. Do not share this OTP.",
                "status" to "PENDING",
                "createdAt" to FieldValue.serverTimestamp(),
                "latencyMs" to 0L,
                "expiresAtEpochMs" to expiresAt,
                "isVerified" to false
            )
            docRef.set(payload).await()

            // Atomically increment quota usage on matching API key
            try {
                val matchingKey = db.collection("api_keys")
                    .whereEqualTo("apiKey", apiKey)
                    .limit(1)
                    .get()
                    .await()
                matchingKey.documents.firstOrNull()?.reference?.update("totalDispatched", FieldValue.increment(1))
            } catch (e: Exception) {
                Log.w("PulseGateway", "Could not increment apiKey quota: ${e.message}")
            }

            true
        } catch (e: Exception) {
            Log.e("PulseGateway", "Failed to dispatch test OTP", e)
            false
        }
    }

    /**
     * Accurate Gateway OTP Code Verification
     */
    suspend fun verifyOtpCode(
        code: String,
        targetPhone: String? = null
    ): OtpVerificationResult = withContext(Dispatchers.IO) {
        val cleanCode = code.trim()
        if (cleanCode.isBlank()) return@withContext OtpVerificationResult.InvalidOrNotFound

        try {
            val query = db.collection("gateway_dispatches")
                .whereEqualTo("otpCode", cleanCode)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)

            val snapshot = query.get().await()
            val matchingDoc = snapshot.documents.firstOrNull { doc ->
                if (targetPhone.isNullOrBlank()) true else doc.getString("recipientPhone") == targetPhone.trim()
            } ?: return@withContext OtpVerificationResult.InvalidOrNotFound

            val dispatch = matchingDoc.toObject(GatewayDispatch::class.java)
                ?: return@withContext OtpVerificationResult.InvalidOrNotFound

            if (dispatch.isExpired) {
                return@withContext OtpVerificationResult.Expired
            }

            // Mark document as verified
            matchingDoc.reference.update(
                mapOf(
                    "isVerified" to true,
                    "verifiedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            return@withContext OtpVerificationResult.Success(
                senderName = dispatch.senderName,
                otpCode = dispatch.otpCode,
                latencyMs = dispatch.latencyMs.coerceAtLeast(140L)
            )
        } catch (e: Exception) {
            Log.e("PulseGateway", "Verification check failed", e)
            return@withContext OtpVerificationResult.Error(e.localizedMessage ?: "Verification error")
        }
    }
}
