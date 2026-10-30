package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_messages")
data class ScheduledMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipientPhone: String,
    val recipientName: String,
    val body: String,
    val scheduledTime: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = STATUS_PENDING // "PENDING", "SENT", "CANCELLED"
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_SENT = "SENT"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}
