package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.GatewayDispatch
import com.example.model.SmsMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Pulse", appName)
    }

    @Test
    fun `detect otp from message body`() {
        val message = SmsMessage(
            id = 1L,
            threadId = 1L,
            address = "BANK-ALERT",
            body = "Your verification code is 849201 for online banking login.",
            date = System.currentTimeMillis(),
            type = 1,
            read = true
        )
        assertTrue(message.isOtp)
        assertEquals("849201", message.extractedOtp)
    }

    @Test
    fun `gateway dispatch model verification`() {
        val dispatch = GatewayDispatch(
            dispatchId = "test_disp_1",
            apiKey = "pulse_live_test_1234",
            senderName = "Acme Corp",
            recipientPhone = "+15550192831",
            otpCode = "948201",
            body = "Acme Corp: Your verification code is 948201",
            status = "DELIVERED",
            latencyMs = 145L,
            expiresAtEpochMs = System.currentTimeMillis() + 300000L,
            isVerified = false
        )
        assertEquals("Acme Corp", dispatch.senderName)
        assertEquals("948201", dispatch.otpCode)
        assertEquals("DELIVERED", dispatch.status)
        assertEquals(145L, dispatch.latencyMs)
        org.junit.Assert.assertFalse(dispatch.isExpired)
        org.junit.Assert.assertFalse(dispatch.isVerified)
    }

    @Test
    fun `extract otp from notification helper`() {
        val otp1 = com.example.notification.PulseNotificationHelper.extractOtp("Use verification code 482910 to confirm login.")
        assertEquals("482910", otp1)

        val otp2 = com.example.notification.PulseNotificationHelper.extractOtp("Your Google OTP is 192842. Do not share.")
        assertEquals("192842", otp2)

        val nonOtp = com.example.notification.PulseNotificationHelper.extractOtp("Hey, are you free for lunch tomorrow at 12?")
        org.junit.Assert.assertNull(nonOtp)
    }

    @Test
    fun `scheduled message entity model creation`() {
        val entity = com.example.data.local.ScheduledMessageEntity(
            id = 101L,
            recipientPhone = "+12025550198",
            recipientName = "Alice",
            body = "Happy Birthday! Have an amazing day!",
            scheduledTime = System.currentTimeMillis() + 3600000L,
            status = "PENDING"
        )
        assertEquals("+12025550198", entity.recipientPhone)
        assertEquals("PENDING", entity.status)
        assertTrue(entity.scheduledTime > System.currentTimeMillis())
    }
}
