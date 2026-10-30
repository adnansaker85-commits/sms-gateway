package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SmsThread(
    val threadId: Long,
    val address: String,
    val displayName: String,
    val snippet: String,
    val date: Long,
    val unreadCount: Int,
    val messageCount: Int,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val category: CategoryTab = CategoryTab.PERSONAL
) {
    val formattedDate: String get() {
        val messageDate = Date(date)
        val now = System.currentTimeMillis()
        val diff = now - date

        return when {
            diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(messageDate)
            diff < 48 * 60 * 60 * 1000 -> "Yesterday"
            diff < 7 * 24 * 60 * 60 * 1000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(messageDate)
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(messageDate)
        }
    }

    val initials: String get() {
        val cleanName = displayName.trim()
        if (cleanName.isEmpty()) return "?"
        val parts = cleanName.split(" ").filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            parts.isNotEmpty() -> cleanName.take(2).uppercase()
            else -> "?"
        }
    }
}
