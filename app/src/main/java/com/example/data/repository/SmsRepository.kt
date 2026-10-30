package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import com.example.data.local.BlockedNumberEntity
import com.example.data.local.PulseDatabase
import com.example.data.local.ScheduledMessageEntity
import com.example.data.local.StarredMessageEntity
import com.example.data.local.ThreadMetaEntity
import com.example.model.CategoryTab
import com.example.model.SmsMessage
import com.example.model.SmsThread
import com.example.receiver.ScheduledSmsReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SmsRepository(private val context: Context) {

    private val contactsHelper = ContactsHelper(context)
    private val database = PulseDatabase.getInstance(context)
    private val threadMetaDao = database.threadMetaDao()
    private val starredDao = database.starredMessageDao()
    private val scheduledDao = database.scheduledMessageDao()
    private val blockedDao = database.blockedNumberDao()

    /**
     * Real-time flow of SMS conversations, driven by ContentObserver
     */
    fun observeConversations(): Flow<List<SmsThread>> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                Log.d("PulseSmsRepo", "ContentObserver triggered for URI: $uri")
                trySend(loadConversationsSync())
            }
        }

        try {
            context.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                observer
            )
        } catch (e: Exception) {
            Log.e("PulseSmsRepo", "Failed to register ContentObserver", e)
        }

        // Emit initial data
        trySend(loadConversationsSync())

        awaitClose {
            try {
                context.contentResolver.unregisterContentObserver(observer)
            } catch (e: Exception) {
                Log.e("PulseSmsRepo", "Failed to unregister ContentObserver", e)
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Real-time flow of messages for a single thread
     */
    fun observeMessagesForThread(threadId: Long): Flow<List<SmsMessage>> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                trySend(loadMessagesForThreadSync(threadId))
            }
        }

        try {
            context.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                observer
            )
        } catch (e: Exception) {
            Log.e("PulseSmsRepo", "Failed to register thread ContentObserver", e)
        }

        trySend(loadMessagesForThreadSync(threadId))

        awaitClose {
            try {
                context.contentResolver.unregisterContentObserver(observer)
            } catch (e: Exception) {
                Log.e("PulseSmsRepo", "Failed to unregister thread ContentObserver", e)
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getConversations(): List<SmsThread> = withContext(Dispatchers.IO) {
        loadConversationsSync()
    }

    suspend fun getMessagesForThread(threadId: Long): List<SmsMessage> = withContext(Dispatchers.IO) {
        loadMessagesForThreadSync(threadId)
    }

    private fun loadConversationsSync(): List<SmsThread> {
        val threads = mutableListOf<SmsThread>()
        val threadMetaMap = mutableMapOf<Long, ThreadMetaEntity>()

        try {
            // Read local metadata
            val metaList = database.openHelper.readableDatabase.query(
                "SELECT threadId, isPinned, isArchived, draft FROM thread_meta"
            )
            metaList.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val pinned = cursor.getInt(1) == 1
                    val archived = cursor.getInt(2) == 1
                    val draft = cursor.getString(3)
                    threadMetaMap[id] = ThreadMetaEntity(id, pinned, archived, draft)
                }
            }
        } catch (e: Exception) {
            Log.w("PulseSmsRepo", "Could not query thread_meta table directly", e)
        }

        try {
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.READ
            )

            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            val seenThreads = mutableSetOf<Long>()
            val threadUnreadMap = mutableMapOf<Long, Int>()
            val threadCountMap = mutableMapOf<Long, Int>()

            cursor?.use {
                val threadIdIdx = it.getColumnIndex(Telephony.Sms.THREAD_ID)
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
                val readIdx = it.getColumnIndex(Telephony.Sms.READ)

                while (it.moveToNext()) {
                    val threadId = if (threadIdIdx != -1) it.getLong(threadIdIdx) else 0L
                    val address = if (addressIdx != -1) it.getString(addressIdx) ?: "Unknown" else "Unknown"
                    val body = if (bodyIdx != -1) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else 0L
                    val read = if (readIdx != -1) it.getInt(readIdx) == 1 else true

                    threadCountMap[threadId] = (threadCountMap[threadId] ?: 0) + 1
                    if (!read) {
                        threadUnreadMap[threadId] = (threadUnreadMap[threadId] ?: 0) + 1
                    }

                    if (!seenThreads.contains(threadId)) {
                        seenThreads.add(threadId)
                        val meta = threadMetaMap[threadId]
                        val displayName = contactsHelper.resolveContactName(address)
                        val category = determineCategory(address, body, meta?.isArchived == true)

                        threads.add(
                            SmsThread(
                                threadId = threadId,
                                address = address,
                                displayName = displayName,
                                snippet = body,
                                date = date,
                                unreadCount = 0, // Will update below
                                messageCount = 0,
                                isPinned = meta?.isPinned ?: false,
                                isArchived = meta?.isArchived ?: false,
                                category = category
                            )
                        )
                    }
                }
            }

            // Map unread counts and message counts, and sort pinned threads first
            val finalThreads = threads.map { thread ->
                thread.copy(
                    unreadCount = threadUnreadMap[thread.threadId] ?: 0,
                    messageCount = threadCountMap[thread.threadId] ?: 1
                )
            }.sortedWith(compareByDescending<SmsThread> { it.isPinned }.thenByDescending { it.date })

            return finalThreads
        } catch (e: Exception) {
            Log.e("PulseSmsRepo", "Error reading SMS conversations", e)
            return emptyList()
        }
    }

    private fun loadMessagesForThreadSync(threadId: Long): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        val starredIds = mutableSetOf<Long>()

        try {
            val cursor = database.openHelper.readableDatabase.query("SELECT messageId FROM starred_messages")
            cursor.use {
                while (it.moveToNext()) {
                    starredIds.add(it.getLong(0))
                }
            }
        } catch (e: Exception) {
            Log.w("PulseSmsRepo", "Could not read starred_messages directly", e)
        }

        try {
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.THREAD_ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.READ
            )

            val selection = "${Telephony.Sms.THREAD_ID} = ?"
            val selectionArgs = arrayOf(threadId.toString())

            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${Telephony.Sms.DATE} ASC"
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(Telephony.Sms._ID)
                val threadIdIdx = it.getColumnIndex(Telephony.Sms.THREAD_ID)
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
                val typeIdx = it.getColumnIndex(Telephony.Sms.TYPE)
                val readIdx = it.getColumnIndex(Telephony.Sms.READ)

                while (it.moveToNext()) {
                    val id = if (idIdx != -1) it.getLong(idIdx) else 0L
                    val tId = if (threadIdIdx != -1) it.getLong(threadIdIdx) else threadId
                    val address = if (addressIdx != -1) it.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx != -1) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()
                    val type = if (typeIdx != -1) it.getInt(typeIdx) else 1
                    val read = if (readIdx != -1) it.getInt(readIdx) == 1 else true

                    messages.add(
                        SmsMessage(
                            id = id,
                            threadId = tId,
                            address = address,
                            body = body,
                            date = date,
                            type = type,
                            read = read,
                            isStarred = starredIds.contains(id)
                        )
                    )
                }
            }
            return messages
        } catch (e: Exception) {
            Log.e("PulseSmsRepo", "Error reading messages for thread $threadId", e)
            return emptyList()
        }
    }

    suspend fun sendSms(destinationAddress: String, messageText: String, threadId: Long?): Boolean = withContext(Dispatchers.IO) {
        if (destinationAddress.isBlank() || messageText.isBlank()) return@withContext false

        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            // Split into parts if length exceeds 160 characters
            val parts = smsManager.divideMessage(messageText)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(destinationAddress, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(destinationAddress, null, messageText, null, null)
            }

            // Write to Sent SMS provider
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, destinationAddress)
                put(Telephony.Sms.BODY, messageText)
                put(Telephony.Sms.DATE, System.currentTimeMillis())
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                if (threadId != null && threadId > 0) {
                    put(Telephony.Sms.THREAD_ID, threadId)
                }
            }
            context.contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
            true
        } catch (e: Exception) {
            Log.e("PulseSmsRepo", "Failed to send SMS to $destinationAddress", e)
            false
        }
    }

    suspend fun markThreadAsRead(threadId: Long) = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.READ, 1)
            }
            context.contentResolver.update(
                Telephony.Sms.CONTENT_URI,
                values,
                "${Telephony.Sms.THREAD_ID} = ? AND ${Telephony.Sms.READ} = 0",
                arrayOf(threadId.toString())
            )
        } catch (e: Exception) {
            Log.e("PulseSmsRepo", "Error marking thread as read", e)
        }
    }

    suspend fun togglePinThread(threadId: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        val current = threadMetaDao.getMetaForThread(threadId)
        if (current == null) {
            threadMetaDao.insertOrUpdate(ThreadMetaEntity(threadId = threadId, isPinned = isPinned))
        } else {
            threadMetaDao.setPinned(threadId, isPinned)
        }
    }

    suspend fun toggleArchiveThread(threadId: Long, isArchived: Boolean) = withContext(Dispatchers.IO) {
        val current = threadMetaDao.getMetaForThread(threadId)
        if (current == null) {
            threadMetaDao.insertOrUpdate(ThreadMetaEntity(threadId = threadId, isArchived = isArchived))
        } else {
            threadMetaDao.setArchived(threadId, isArchived)
        }
    }

    suspend fun toggleStarMessage(message: SmsMessage) = withContext(Dispatchers.IO) {
        if (message.isStarred) {
            starredDao.deleteStarred(message.id)
        } else {
            starredDao.insertStarred(
                StarredMessageEntity(
                    messageId = message.id,
                    threadId = message.threadId,
                    address = message.address,
                    body = message.body,
                    date = message.date
                )
            )
        }
    }

    fun observeStarredMessages(): Flow<List<StarredMessageEntity>> = starredDao.getAllStarred()

    fun searchContacts(query: String) = contactsHelper.searchContacts(query)

    fun resolveName(address: String) = contactsHelper.resolveContactName(address)

    private fun determineCategory(address: String, body: String, isArchived: Boolean): CategoryTab {
        if (isArchived) return CategoryTab.ARCHIVED

        val cleanAddress = address.trim()
        val isAlphanumericSender = cleanAddress.any { it.isLetter() }
        val isShortCode = cleanAddress.replace("+", "").length in 3..6 && cleanAddress.all { it.isDigit() || it == '+' }

        val hasOtpKeywords = body.contains("OTP", ignoreCase = true) ||
                body.contains("verification code", ignoreCase = true) ||
                body.contains("one-time", ignoreCase = true) ||
                body.contains("bank", ignoreCase = true) ||
                body.contains("debited", ignoreCase = true) ||
                body.contains("credited", ignoreCase = true) ||
                body.contains("security code", ignoreCase = true)

        val hasUpdateKeywords = body.contains("newsletter", ignoreCase = true) ||
                body.contains("promo", ignoreCase = true) ||
                body.contains("discount", ignoreCase = true) ||
                body.contains("offer", ignoreCase = true) ||
                body.contains("subscribe", ignoreCase = true)

        return when {
            isAlphanumericSender && hasOtpKeywords -> CategoryTab.TRANSACTIONS
            isShortCode && hasOtpKeywords -> CategoryTab.TRANSACTIONS
            hasOtpKeywords -> CategoryTab.TRANSACTIONS
            isAlphanumericSender && hasUpdateKeywords -> CategoryTab.UPDATES
            isAlphanumericSender || isShortCode -> CategoryTab.UPDATES
            else -> CategoryTab.PERSONAL
        }
    }

    // Scheduled SMS Methods
    fun scheduleMessage(
        recipientPhone: String,
        recipientName: String,
        body: String,
        scheduledTime: Long,
        onScheduled: (Long) -> Unit
    ) {
        val entity = ScheduledMessageEntity(
            recipientPhone = recipientPhone,
            recipientName = recipientName,
            body = body,
            scheduledTime = scheduledTime,
            status = ScheduledMessageEntity.STATUS_PENDING
        )
        ScheduledSmsReceiver.schedule(context, entity, onScheduled)
    }

    fun cancelScheduledMessage(id: Long) {
        ScheduledSmsReceiver.cancel(context, id)
    }

    fun observeScheduledForRecipient(phone: String): Flow<List<ScheduledMessageEntity>> =
        scheduledDao.getPendingForRecipient(phone)

    fun observeAllPendingScheduled(): Flow<List<ScheduledMessageEntity>> =
        scheduledDao.getPendingScheduled()

    // Blocked Numbers & Spam Shield
    suspend fun blockNumber(phone: String, name: String = "", reason: String = "Spam") = withContext(Dispatchers.IO) {
        blockedDao.insert(BlockedNumberEntity(phone = phone, name = name, reason = reason))
    }

    suspend fun unblockNumber(phone: String) = withContext(Dispatchers.IO) {
        blockedDao.unblock(phone)
    }

    fun observeBlockedNumbers(): Flow<List<BlockedNumberEntity>> = blockedDao.getAllBlocked()

    suspend fun isNumberBlocked(phone: String): Boolean = withContext(Dispatchers.IO) {
        blockedDao.isBlocked(phone)
    }

    // Auto-Purge Expired OTPs
    suspend fun purgeExpiredOtps(olderThanHours: Long = 24): Int = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - (olderThanHours * 3600_000L)
        var deletedCount = 0
        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms._ID, Telephony.Sms.BODY, Telephony.Sms.DATE),
                "${Telephony.Sms.DATE} < ?",
                arrayOf(cutoffTime.toString()),
                null
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(Telephony.Sms._ID)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)

                while (it.moveToNext()) {
                    val id = it.getLong(idIdx)
                    val body = it.getString(bodyIdx) ?: ""
                    val isOtp = com.example.notification.PulseNotificationHelper.extractOtp(body) != null

                    if (isOtp) {
                        val uri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI, id.toString())
                        val rows = context.contentResolver.delete(uri, null, null)
                        if (rows > 0) deletedCount++
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PulseSmsRepo", "Error purging expired OTPs: ${e.message}")
        }
        deletedCount
    }
}

