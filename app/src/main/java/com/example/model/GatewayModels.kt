package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class BusinessAccount(
    @DocumentId val userId: String = "",
    val organizationName: String = "",
    val contactEmail: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val isVerified: Boolean = true
)

@IgnoreExtraProperties
data class ApiKeyItem(
    @DocumentId val keyId: String = "",
    val userId: String = "",
    val appName: String = "",
    val apiKey: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val isActive: Boolean = true,
    val totalDispatched: Long = 0L
)

@IgnoreExtraProperties
data class GatewayDispatch(
    @DocumentId val dispatchId: String = "",
    val apiKey: String = "",
    val senderName: String = "",
    val recipientPhone: String = "",
    val otpCode: String = "",
    val body: String = "",
    val status: String = "PENDING", // PENDING, DELIVERED, FAILED
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val deliveredAt: Timestamp? = null,
    val latencyMs: Long = 0L,
    val expiresAtEpochMs: Long = 0L,
    val isVerified: Boolean = false,
    @ServerTimestamp val verifiedAt: Timestamp? = null
) {
    val isExpired: Boolean
        get() = if (expiresAtEpochMs > 0) System.currentTimeMillis() > expiresAtEpochMs else false
}
