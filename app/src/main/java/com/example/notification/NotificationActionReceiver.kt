package com.example.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.repository.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = intent.getIntExtra(PulseNotificationHelper.EXTRA_NOTIFICATION_ID, 0)

        when (intent.action) {
            PulseNotificationHelper.ACTION_COPY_OTP -> {
                val otpCode = intent.getStringExtra(PulseNotificationHelper.EXTRA_OTP_CODE)
                if (!otpCode.isNullOrBlank()) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("OTP Code", otpCode)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(
                        context,
                        "Copied verification code: $otpCode",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Dismiss notification after copying
                    notificationManager.cancel(notificationId)
                }
            }

            PulseNotificationHelper.ACTION_MARK_READ -> {
                val threadId = intent.getLongExtra(PulseNotificationHelper.EXTRA_THREAD_ID, 0L)
                if (threadId > 0) {
                    val repository = SmsRepository(context)
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.markThreadAsRead(threadId)
                    }
                }
                notificationManager.cancel(notificationId)
                Toast.makeText(context, "Marked as read", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
