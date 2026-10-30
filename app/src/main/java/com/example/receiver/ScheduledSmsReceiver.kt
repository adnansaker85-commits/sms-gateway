package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.local.PulseDatabase
import com.example.data.local.ScheduledMessageEntity
import com.example.data.repository.SmsRepository
import com.example.notification.PulseNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduledSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val messageId = intent?.getLongExtra(EXTRA_MESSAGE_ID, -1L) ?: return
        if (messageId == -1L) return

        Log.d("ScheduledSmsReceiver", "Alarm triggered for scheduled message #$messageId")

        val database = PulseDatabase.getInstance(context)
        val dao = database.scheduledMessageDao()
        val repository = SmsRepository(context)

        CoroutineScope(Dispatchers.IO).launch {
            val item = dao.getById(messageId)
            if (item != null && item.status == ScheduledMessageEntity.STATUS_PENDING) {
                val success = repository.sendSms(item.recipientPhone, item.body, null)
                if (success) {
                    dao.updateStatus(messageId, ScheduledMessageEntity.STATUS_SENT)
                    showDeliveryConfirmationNotification(context, item)
                } else {
                    dao.updateStatus(messageId, ScheduledMessageEntity.STATUS_CANCELLED)
                }
            }
        }
    }

    private fun showDeliveryConfirmationNotification(context: Context, item: ScheduledMessageEntity) {
        PulseNotificationHelper.createNotificationChannel(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, PulseNotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Scheduled Message Delivered")
            .setContentText("Sent to ${item.recipientName.ifBlank { item.recipientPhone }}: ${item.body}")
            .setAutoCancel(true)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }

    companion object {
        const val EXTRA_MESSAGE_ID = "extra_scheduled_message_id"

        fun schedule(context: Context, entity: ScheduledMessageEntity, onComplete: (Long) -> Unit) {
            val db = PulseDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch {
                val newId = db.scheduledMessageDao().insert(entity)

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, ScheduledSmsReceiver::class.java).apply {
                    putExtra(EXTRA_MESSAGE_ID, newId)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    newId.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            entity.scheduledTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            entity.scheduledTime,
                            pendingIntent
                        )
                    }
                } catch (e: Exception) {
                    Log.w("ScheduledSmsReceiver", "Could not set exact alarm, falling back to set: ${e.message}")
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        entity.scheduledTime,
                        pendingIntent
                    )
                }

                onComplete(newId)
            }
        }

        fun cancel(context: Context, messageId: Long) {
            val db = PulseDatabase.getInstance(context)
            CoroutineScope(Dispatchers.IO).launch {
                db.scheduledMessageDao().updateStatus(messageId, ScheduledMessageEntity.STATUS_CANCELLED)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, ScheduledSmsReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    messageId.toInt(),
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
    }
}
