package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class SmsMessage(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val type: Int, // 1 = Inbox, 2 = Sent, 3 = Draft
    val read: Boolean,
    val isStarred: Boolean = false
) {
    val isSent: Boolean get() = type == 2 || type == 4

    val formattedTime: String get() {
        val messageDate = Date(date)
        val now = System.currentTimeMillis()
        val diff = now - date

        return when {
            diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(messageDate)
            diff < 7 * 24 * 60 * 60 * 1000 -> SimpleDateFormat("EEE h:mm a", Locale.getDefault()).format(messageDate)
            else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(messageDate)
        }
    }

    val extractedOtp: String? get() {
        // Find 4 to 8 digit numerical OTP or alphanumeric security code
        val otpPattern = Pattern.compile("(?i)(?:code|otp|passcode|secret|pin|verification)\\D{0,10}(\\b[0-9]{4,8}\\b)")
        val matcher = otpPattern.matcher(body)
        if (matcher.find()) {
            return matcher.group(1)
        }
        // Fallback for standalone 4-6 digit standalone tokens in transactional messages
        val digitsOnlyPattern = Pattern.compile("\\b[0-9]{4,6}\\b")
        val digitMatcher = digitsOnlyPattern.matcher(body)
        if (digitMatcher.find() && (body.contains("OTP", ignoreCase = true) || body.contains("verification", ignoreCase = true) || body.contains("code", ignoreCase = true))) {
            return digitMatcher.group(0)
        }
        return null
    }

    val isOtp: Boolean get() = extractedOtp != null
}
