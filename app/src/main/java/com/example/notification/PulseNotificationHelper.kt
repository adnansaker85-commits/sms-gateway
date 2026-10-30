package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.MainActivity
import com.example.R
import java.util.regex.Pattern

object PulseNotificationHelper {

    const val CHANNEL_ID = "pulse_messages_channel"
    const val CHANNEL_NAME = "Messages & Verification OTPs"
    const val CHANNEL_DESC = "Real-time alerts for incoming SMS and zero-fee Gateway OTPs"

    const val ACTION_COPY_OTP = "com.example.notification.ACTION_COPY_OTP"
    const val ACTION_MARK_READ = "com.example.notification.ACTION_MARK_READ"
    const val ACTION_DIRECT_REPLY = "com.example.notification.ACTION_DIRECT_REPLY"

    const val EXTRA_OTP_CODE = "extra_otp_code"
    const val EXTRA_THREAD_ID = "extra_thread_id"
    const val EXTRA_ADDRESS = "extra_address"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    const val KEY_TEXT_REPLY = "key_text_reply"

    private val OTP_REGEX = Pattern.compile("\\b(\\d{4,8})\\b")
    private val OTP_KEYWORDS = listOf("code", "otp", "verification", "verify", "pin", "password", "one-time", "security")

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun extractOtp(body: String): String? {
        val lower = body.lowercase()
        val hasKeyword = OTP_KEYWORDS.any { lower.contains(it) }
        if (!hasKeyword) return null

        val matcher = OTP_REGEX.matcher(body)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            null
        }
    }

    fun showMessageNotification(
        context: Context,
        senderName: String,
        address: String,
        body: String,
        threadId: Long
    ) {
        createNotificationChannel(context)

        val notificationId = (address.hashCode() and 0x7FFFFFFF)
        val extractedOtp = extractOtp(body)

        // 1. Content tap intent -> opens conversation
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_THREAD_ID, threadId)
            putExtra(EXTRA_ADDRESS, address)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(senderName)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        // 2. Add Action: One-Tap Copy OTP Code (if OTP is detected)
        if (!extractedOtp.isNullOrBlank()) {
            val copyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_COPY_OTP
                putExtra(EXTRA_OTP_CODE, extractedOtp)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            val copyPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 1,
                copyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val copyAction = NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "📋 Copy Code $extractedOtp",
                copyPendingIntent
            ).build()

            notificationBuilder.addAction(copyAction)
        }

        // 3. Add Action: Inline Direct Reply (RemoteInput)
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Type a reply...")
            .build()

        val replyIntent = Intent(context, DirectReplyReceiver::class.java).apply {
            action = ACTION_DIRECT_REPLY
            putExtra(EXTRA_ADDRESS, address)
            putExtra(EXTRA_THREAD_ID, threadId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            "💬 Reply",
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()

        notificationBuilder.addAction(replyAction)

        // 4. Add Action: Mark as Read
        val markReadIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_READ
            putExtra(EXTRA_THREAD_ID, threadId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 3,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markReadAction = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            "✓ Mark as Read",
            markReadPendingIntent
        ).build()

        notificationBuilder.addAction(markReadAction)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
