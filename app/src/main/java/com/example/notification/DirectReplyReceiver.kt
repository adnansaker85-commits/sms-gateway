package com.example.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.R
import com.example.data.repository.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DirectReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val results = RemoteInput.getResultsFromIntent(intent) ?: return
        val replyText = results.getCharSequence(PulseNotificationHelper.KEY_TEXT_REPLY)?.toString()?.trim()

        val address = intent.getStringExtra(PulseNotificationHelper.EXTRA_ADDRESS) ?: return
        val threadId = intent.getLongExtra(PulseNotificationHelper.EXTRA_THREAD_ID, 0L)
        val notificationId = intent.getIntExtra(PulseNotificationHelper.EXTRA_NOTIFICATION_ID, 0)

        if (!replyText.isNullOrBlank()) {
            val repository = SmsRepository(context)
            CoroutineScope(Dispatchers.IO).launch {
                val sent = repository.sendSms(address, replyText, null)
                if (sent) {
                    repository.markThreadAsRead(threadId)
                }
            }

            // Update notification with delivery confirmation
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val repliedNotification = NotificationCompat.Builder(context, PulseNotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Sent to $address")
                .setContentText(replyText)
                .setAutoCancel(true)
                .setTimeoutAfter(3000)
                .build()

            notificationManager.notify(notificationId, repliedNotification)
            Toast.makeText(context, "Reply sent to $address", Toast.LENGTH_SHORT).show()
        }
    }
}
