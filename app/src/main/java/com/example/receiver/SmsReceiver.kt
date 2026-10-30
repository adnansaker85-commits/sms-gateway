package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            try {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (!messages.isNullOrEmpty()) {
                    val sender = messages[0].displayOriginatingAddress ?: "Unknown"
                    val fullBody = messages.joinToString("") { it.displayMessageBody ?: "" }
                    Log.d("PulseSmsReceiver", "Real-time SMS received from $sender: $fullBody")
                    
                    // Emit to live listener bus
                    _liveIncomingSms.tryEmit(IncomingSmsEvent(sender, fullBody, System.currentTimeMillis()))

                    // Show interactive system notification with inline OTP copy and direct reply
                    com.example.notification.PulseNotificationHelper.showMessageNotification(
                        context = context,
                        senderName = sender,
                        address = sender,
                        body = fullBody,
                        threadId = 0L
                    )
                }
            } catch (e: Exception) {
                Log.e("PulseSmsReceiver", "Error parsing incoming SMS", e)
            }
        }
    }

    data class IncomingSmsEvent(
        val sender: String,
        val body: String,
        val timestamp: Long
    )

    companion object {
        private val _liveIncomingSms = MutableSharedFlow<IncomingSmsEvent>(extraBufferCapacity = 64)
        val liveIncomingSms = _liveIncomingSms.asSharedFlow()
    }
}
