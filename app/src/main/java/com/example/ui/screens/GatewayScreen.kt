package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.GatewayRepository
import com.example.model.ApiKeyItem
import com.example.model.BusinessAccount
import com.example.model.GatewayDispatch
import com.example.ui.components.PulseButton
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LightSlate
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PorcelainWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorderHairline
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

@Composable
fun GatewayScreen(
    businessAccount: BusinessAccount?,
    apiKeys: List<ApiKeyItem>,
    dispatches: List<GatewayDispatch>,
    verificationResult: GatewayRepository.OtpVerificationResult? = null,
    onGenerateKey: (appName: String) -> Unit,
    onSimulateDispatch: (appName: String, phone: String, otp: String, apiKey: String) -> Unit,
    onVerifyOtp: (code: String, phone: String?) -> Unit = { _, _ -> },
    onClearVerification: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryApiKey = apiKeys.firstOrNull()?.apiKey ?: "pulse_live_demo_key_7719"
    val orgName = businessAccount?.organizationName ?: "My Business Organization"

    var activeTab by remember { mutableStateOf("Simulator") } // Simulator, Verification, Docs, Logs
    var selectedCodeTab by remember { mutableStateOf("cURL") }

    // Simulator states
    var simAppName by remember { mutableStateOf(orgName) }
    var simPhone by remember { mutableStateOf("+1 555 019 2831") }
    var otpLength by remember { mutableStateOf(6) }
    var simOtp by remember { mutableStateOf(generateRandomCode(6)) }
    var isSimulating by remember { mutableStateOf(false) }

    // Verification Tester states
    var verifyCodeInput by remember { mutableStateOf("") }
    var verifyPhoneInput by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    val totalDelivered = dispatches.count { it.status == "DELIVERED" }
    val totalVerified = dispatches.count { it.isVerified }
    val savingsUsd = String.format(Locale.US, "%.2f", (totalDelivered * 0.06))
    val avgLatency = if (dispatches.isNotEmpty()) {
        val nonZero = dispatches.map { it.latencyMs }.filter { it > 0 }
        if (nonZero.isNotEmpty()) (nonZero.sum() / nonZero.size).coerceAtLeast(130L) else 142L
    } else 142L

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PorcelainWhite),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status & Enterprise Metrics Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = RoundedCornerShape(24.dp),
                        elevation = 8.dp,
                        surfaceColor = PureWhite
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(LiveGreen)
                            )
                            Text(
                                text = "Gateway Online",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepOnyx
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LiveGreen.copy(alpha = 0.12f))
                                .border(1.dp, LiveGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ZERO CARRIER FEES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiveGreen,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "$totalDelivered",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepOnyx,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = "OTPs Dispatched",
                                fontSize = 12.sp,
                                color = LightSlate
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${avgLatency}ms",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiveGreen,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = "Avg Latency",
                                fontSize = 12.sp,
                                color = LightSlate
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$$savingsUsd",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = "Carrier Fees Saved",
                                fontSize = 12.sp,
                                color = LightSlate
                            )
                        }
                    }
                }
            }
        }

        // Active API Key Management Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = RoundedCornerShape(24.dp),
                        elevation = 6.dp,
                        surfaceColor = PureWhite
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = DeepOnyx,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Gateway API Key",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepOnyx
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = orgName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // API Key Token Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceSubtle)
                            .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = primaryApiKey,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepOnyx
                        )

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Pulse API Key", primaryApiKey))
                                Toast.makeText(context, "API Key copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy API Key",
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val activeKey = apiKeys.firstOrNull { it.apiKey == primaryApiKey }
                        Text(
                            text = "Dispatched: ${activeKey?.totalDispatched ?: totalDelivered} requests",
                            fontSize = 12.sp,
                            color = LightSlate
                        )

                        Text(
                            text = "Regenerate Key",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            modifier = Modifier
                                .clickable { onGenerateKey(orgName) }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        // Section Selector Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PureWhite)
                    .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    "Simulator" to "Send OTP",
                    "Verification" to "Verify OTP",
                    "Docs" to "API Code",
                    "Logs" to "Live Logs"
                ).forEach { (tabKey, tabLabel) ->
                    val isSelected = activeTab == tabKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) DeepOnyx else Color.Transparent)
                            .clickable { activeTab = tabKey }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabLabel,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PureWhite else MutedSlate
                        )
                    }
                }
            }
        }

        // TAB 1: SEND OTP SIMULATOR
        if (activeTab == "Simulator") {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(24.dp),
                            elevation = 6.dp,
                            surfaceColor = PureWhite
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Dispatch Test OTP via Gateway",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepOnyx
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Simulate an external company app sending a zero-fee OTP. The code will trigger an instant Android system notification with a one-tap copy button.",
                            fontSize = 13.sp,
                            color = MutedSlate,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Form Inputs
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Sender App Name
                            Column {
                                Text(
                                    text = "Sender / Brand Name",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightSlate,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceSubtle)
                                        .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = simAppName,
                                        onValueChange = { simAppName = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            color = DeepOnyx,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        singleLine = true
                                    )
                                }
                            }

                            // Target Phone
                            Column {
                                Text(
                                    text = "Recipient Phone Number",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightSlate,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceSubtle)
                                        .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = simPhone,
                                        onValueChange = { simPhone = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            color = DeepOnyx,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        singleLine = true
                                    )
                                }
                            }

                            // OTP Code & Length Selector
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Generated OTP Code",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightSlate,
                                        letterSpacing = 0.5.sp
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(4, 6, 8).forEach { len ->
                                            val isLenSelected = otpLength == len
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isLenSelected) DeepOnyx else SurfaceSubtle)
                                                    .clickable {
                                                        otpLength = len
                                                        simOtp = generateRandomCode(len)
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${len}D",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isLenSelected) PureWhite else MutedSlate
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceSubtle)
                                        .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = simOtp,
                                        onValueChange = { simOtp = it },
                                        modifier = Modifier.weight(1f),
                                        textStyle = TextStyle(
                                            fontSize = 18.sp,
                                            color = DeepOnyx,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        ),
                                        singleLine = true
                                    )

                                    IconButton(
                                        onClick = { simOtp = generateRandomCode(otpLength) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Generate New OTP",
                                            tint = AccentBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Validity info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = LightSlate,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Valid for 5 minutes • Delivered via direct cloud push",
                                fontSize = 11.sp,
                                color = LightSlate
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        PulseButton(
                            text = if (isSimulating) "Dispatching to Phone..." else "Dispatch OTP via Gateway",
                            onClick = {
                                isSimulating = true
                                onSimulateDispatch(simAppName, simPhone, simOtp, primaryApiKey)
                                Toast.makeText(
                                    context,
                                    "OTP $simOtp dispatched! Check notification shade.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isSimulating = false
                                simOtp = generateRandomCode(otpLength)
                            },
                            icon = Icons.Default.Send,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gateway_dispatch_button")
                        )
                    }
                }
            }
        }

        // TAB 2: OTP VERIFICATION TESTER
        if (activeTab == "Verification") {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(24.dp),
                            elevation = 6.dp,
                            surfaceColor = PureWhite
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = LiveGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "OTP Code Verification Tester",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepOnyx
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Test the accuracy of Pulse Gateway's verification engine. Enter an OTP code received via notification to verify its authenticity and check latency.",
                            fontSize = 13.sp,
                            color = MutedSlate,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Verification Input
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column {
                                Text(
                                    text = "Enter Received OTP Code",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightSlate,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceSubtle)
                                        .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = verifyCodeInput,
                                        onValueChange = {
                                            verifyCodeInput = it
                                            onClearVerification()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(
                                            fontSize = 20.sp,
                                            color = DeepOnyx,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        ),
                                        singleLine = true
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Target Phone (Optional)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightSlate,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceSubtle)
                                        .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = verifyPhoneInput,
                                        onValueChange = { verifyPhoneInput = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(
                                            fontSize = 13.sp,
                                            color = DeepOnyx,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        singleLine = true
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        PulseButton(
                            text = if (isVerifying) "Verifying..." else "Verify Code Authenticity",
                            onClick = {
                                if (verifyCodeInput.isNotBlank()) {
                                    isVerifying = true
                                    onVerifyOtp(verifyCodeInput, verifyPhoneInput.ifBlank { null })
                                    isVerifying = false
                                } else {
                                    Toast.makeText(context, "Please enter an OTP code", Toast.LENGTH_SHORT).show()
                                }
                            },
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Verification Result Card
                        if (verificationResult != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            when (verificationResult) {
                                is GatewayRepository.OtpVerificationResult.Success -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(LiveGreen.copy(alpha = 0.1f))
                                            .border(1.dp, LiveGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = LiveGreen,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "OTP Verified Successfully!",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepOnyx
                                                )
                                                Text(
                                                    text = "Origin: ${verificationResult.senderName} • Latency: ${verificationResult.latencyMs}ms • Status: VALID",
                                                    fontSize = 12.sp,
                                                    color = LiveGreen
                                                )
                                            }
                                        }
                                    }
                                }

                                is GatewayRepository.OtpVerificationResult.Expired -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Timer,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Verification Failed: Code Expired",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepOnyx
                                                )
                                                Text(
                                                    text = "This OTP was created more than 5 minutes ago and is no longer valid.",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFB45309)
                                                )
                                            }
                                        }
                                    }
                                }

                                is GatewayRepository.OtpVerificationResult.InvalidOrNotFound -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Invalid OTP Code",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepOnyx
                                                )
                                                Text(
                                                    text = "No active matching OTP dispatch found for '$verifyCodeInput'.",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFB91C1C)
                                                )
                                            }
                                        }
                                    }
                                }

                                is GatewayRepository.OtpVerificationResult.Error -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = "Verification Error: ${verificationResult.message}",
                                            fontSize = 12.sp,
                                            color = Color(0xFFB91C1C)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: API INTEGRATION CODE
        if (activeTab == "Docs") {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(24.dp),
                            elevation = 6.dp,
                            surfaceColor = PureWhite
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = DeepOnyx,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "API Integration Code & SDK",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepOnyx
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Language Selector Tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("cURL", "Node.js", "Python", "PHP", "Kotlin").forEach { lang ->
                                val isSelected = selectedCodeTab == lang
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) DeepOnyx else SurfaceSubtle)
                                        .clickable { selectedCodeTab = lang }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = lang,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PureWhite else MutedSlate
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val snippetCode = remember(selectedCodeTab, primaryApiKey, orgName) {
                            when (selectedCodeTab) {
                                "Node.js" -> """
// 1. Send OTP
const sendRes = await fetch("https://pulse-gateway.api/v1/send-otp", {
  method: "POST",
  headers: {
    "Authorization": "Bearer $primaryApiKey",
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    recipientPhone: "+15550192831",
    appName: "$orgName",
    otpCode: "849201"
  })
});
const sendData = await sendRes.json();
console.log("Dispatch status:", sendData.status);

// 2. Verify OTP
const verifyRes = await fetch("https://pulse-gateway.api/v1/verify-otp", {
  method: "POST",
  headers: {
    "Authorization": "Bearer $primaryApiKey",
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    otpCode: "849201",
    recipientPhone: "+15550192831"
  })
});
const verifyData = await verifyRes.json();
console.log("Verified:", verifyData.isVerified);
""".trimIndent()

                                "Python" -> """
import requests

# 1. Send OTP
send_url = "https://pulse-gateway.api/v1/send-otp"
headers = {
    "Authorization": "Bearer $primaryApiKey",
    "Content-Type": "application/json"
}
payload = {
    "recipientPhone": "+15550192831",
    "appName": "$orgName",
    "otpCode": "849201"
}
send_res = requests.post(send_url, json=payload, headers=headers)
print(send_res.json())

# 2. Verify OTP
verify_url = "https://pulse-gateway.api/v1/verify-otp"
verify_res = requests.post(verify_url, json={"otpCode": "849201"}, headers=headers)
print("Verified:", verify_res.json().get("isVerified"))
""".trimIndent()

                                "PHP" -> """
<?php
// 1. Send OTP
${'$'}ch = curl_init("https://pulse-gateway.api/v1/send-otp");
curl_setopt(${'$'}ch, CURLOPT_HTTPHEADER, [
    "Authorization: Bearer $primaryApiKey",
    "Content-Type: application/json"
]);
curl_setopt(${'$'}ch, CURLOPT_POSTFIELDS, json_encode([
    "recipientPhone" => "+15550192831",
    "appName" => "$orgName",
    "otpCode" => "849201"
]));
curl_setopt(${'$'}ch, CURLOPT_RETURNTRANSFER, true);
${'$'}response = curl_exec(${'$'}ch);
curl_close(${'$'}ch);
echo ${'$'}response;
?>
""".trimIndent()

                                "Kotlin" -> """
// Android / Kotlin Ktor Client
val client = HttpClient(CIO)
val response: HttpResponse = client.post("https://pulse-gateway.api/v1/send-otp") {
    header(HttpHeaders.Authorization, "Bearer $primaryApiKey")
    contentType(ContentType.Application.Json)
    setBody(mapOf(
        "recipientPhone" to "+15550192831",
        "appName" to "$orgName",
        "otpCode" to "849201"
    ))
}
println("Delivery status: ${'$'}{response.status}")
""".trimIndent()

                                else -> """
# 1. Send Zero-Fee OTP
curl -X POST https://pulse-gateway.api/v1/send-otp \
  -H "Authorization: Bearer $primaryApiKey" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientPhone": "+15550192831",
    "appName": "$orgName",
    "otpCode": "849201"
  }'

# 2. Verify OTP Authenticity
curl -X POST https://pulse-gateway.api/v1/verify-otp \
  -H "Authorization: Bearer $primaryApiKey" \
  -H "Content-Type: application/json" \
  -d '{
    "otpCode": "849201",
    "recipientPhone": "+15550192831"
  }'
""".trimIndent()
                            }
                        }

                        // Code Viewer Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E222B))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedCodeTab.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "Copy Code",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF60A5FA),
                                        modifier = Modifier
                                            .clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Code Snippet", snippetCode))
                                                Toast.makeText(context, "$selectedCodeTab code copied", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = snippetCode,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // TAB 4: LIVE DISPATCH LOGS & TELEMETRY
        if (activeTab == "Logs" || activeTab == "Simulator") {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REAL-TIME TELEMETRY LOGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightSlate,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "${dispatches.size} total dispatches",
                        fontSize = 11.sp,
                        color = LightSlate
                    )
                }
            }

            if (dispatches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PureWhite)
                            .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No dispatches yet. Use 'Send OTP' above to fire real-time verification alerts.",
                            fontSize = 13.sp,
                            color = MutedSlate
                        )
                    }
                }
            } else {
                items(dispatches, key = { it.dispatchId }) { dispatch ->
                    val timeFormatted = dispatch.createdAt?.toDate()?.let {
                        SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(it)
                    } ?: "Just now"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PureWhite)
                            .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = dispatch.senderName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepOnyx
                                    )
                                    Text(
                                        text = "• $timeFormatted",
                                        fontSize = 11.sp,
                                        color = LightSlate
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (dispatch.isVerified) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(AccentBlue.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "VERIFIED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentBlue
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (dispatch.isExpired) Color(0xFFF1F5F9) else LiveGreen.copy(alpha = 0.12f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (dispatch.isExpired) "EXPIRED" else dispatch.status,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (dispatch.isExpired) LightSlate else LiveGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = dispatch.body,
                                fontSize = 13.sp,
                                color = CharcoalDark,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = LightSlate,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Latency: ${dispatch.latencyMs.coerceAtLeast(130L)}ms",
                                        fontSize = 11.sp,
                                        color = LightSlate
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    // 1-Tap Copy Code
                                    Text(
                                        text = "Copy [${dispatch.otpCode}]",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentBlue,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("OTP Code", dispatch.otpCode))
                                                Toast.makeText(context, "Copied code ${dispatch.otpCode}", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(4.dp)
                                    )

                                    // Quick Verify Code
                                    Text(
                                        text = "Verify Code",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LiveGreen,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                verifyCodeInput = dispatch.otpCode
                                                verifyPhoneInput = dispatch.recipientPhone
                                                activeTab = "Verification"
                                                onVerifyOtp(dispatch.otpCode, dispatch.recipientPhone)
                                            }
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

private fun generateRandomCode(length: Int): String {
    val digits = "0123456789"
    return (1..length)
        .map { digits[Random.nextInt(digits.length)] }
        .joinToString("")
}
