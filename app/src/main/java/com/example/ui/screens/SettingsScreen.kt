package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BusinessAccount
import com.example.ui.components.PulseButton
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LightSlate
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PorcelainWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceBorderStrong
import com.google.firebase.auth.FirebaseUser

@Composable
fun SettingsScreen(
    hasSmsPermission: Boolean,
    currentUser: FirebaseUser?,
    businessAccount: BusinessAccount?,
    onSignInAndConvert: (activity: Activity, orgName: String) -> Unit,
    onOpenGatewayConsole: () -> Unit,
    onSignOut: () -> Unit,
    onPurgeExpiredOtps: () -> Unit = {},
    onExportBackup: () -> Unit = {},
    blockedCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    val carrierName = telephonyManager?.networkOperatorName?.ifBlank { "Mobile Network" } ?: "Carrier Service"

    var orgNameInput by remember { mutableStateOf("") }
    var isUpgrading by remember { mutableStateOf(false) }

    // Interactive user preference toggles
    var otpBannerEnabled by remember { mutableStateOf(true) }
    var autoCopyOtpEnabled by remember { mutableStateOf(true) }
    var hapticFeedbackEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PorcelainWhite),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Business Gateway Card
        item {
            SectionHeader(title = "PULSE BUSINESS GATEWAY", icon = Icons.Default.Hub)

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0x180F172A),
                        spotColor = Color(0x220F172A)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(PureWhite)
                    .border(1.5.dp, SurfaceBorderStrong, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                if (businessAccount != null) {
                    // Already Converted to Business Account
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(LiveGreen.copy(alpha = 0.15f))
                                        .border(1.dp, LiveGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = null,
                                        tint = LiveGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = businessAccount.organizationName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepOnyx
                                    )
                                    Text(
                                        text = "Verified Enterprise Gateway",
                                        fontSize = 12.sp,
                                        color = LiveGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LiveGreen.copy(alpha = 0.15f))
                                    .border(1.dp, LiveGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiveGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "External apps and servers can dispatch real-time OTPs to Pulse with zero carrier fees using your secure API keys.",
                            fontSize = 13.sp,
                            color = MutedSlate,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PulseButton(
                                text = "Open Gateway Console",
                                onClick = onOpenGatewayConsole,
                                icon = Icons.Default.Hub,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account: ${currentUser?.email ?: "Google Account"}",
                                fontSize = 12.sp,
                                color = LightSlate
                            )

                            Text(
                                text = "Sign Out",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(onClick = onSignOut)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    // Not Converted Yet: Upgrade Form
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Convert to Business Account",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepOnyx
                                )
                                Text(
                                    text = "Zero-fee OTP & API Gateway",
                                    fontSize = 12.sp,
                                    color = AccentBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Upgrade your account to generate secure API keys, integrate ready-to-use SDK snippets into your projects, and send free OTPs directly to Pulse.",
                            fontSize = 13.sp,
                            color = MutedSlate,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // High-contrast Organization Name Input
                        Text(
                            text = "COMPANY OR APP NAME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightSlate,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .border(1.5.dp, SurfaceBorderStrong, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = orgNameInput,
                                onValueChange = { orgNameInput = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = DeepOnyx,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (orgNameInput.isEmpty()) {
                                        Text(
                                            text = "e.g. Acme Corp or Fintech Pay",
                                            fontSize = 13.sp,
                                            color = LightSlate
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        PulseButton(
                            text = if (isUpgrading) "Connecting with Google..." else "Sign in with Google & Upgrade",
                            onClick = {
                                if (activity != null) {
                                    isUpgrading = true
                                    onSignInAndConvert(activity, orgNameInput.ifBlank { "My Business App" })
                                }
                            },
                            icon = Icons.Default.Key,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Modular Group 1: Telephony & Live Synchronization
        item {
            SectionHeader(title = "TELEPHONY & SYNC ENGINE", icon = Icons.Default.Sync)

            Spacer(modifier = Modifier.height(8.dp))

            ModularCard {
                ModularSettingRow(
                    icon = Icons.Default.Sync,
                    iconBg = Color(0xFFECFDF5),
                    iconTint = Color(0xFF059669),
                    title = "Real-Time Telephony Sync",
                    subtitle = "Observing Telephony.Sms with native Android ContentObserver",
                    trailing = {
                        StatusBadge(text = "LIVE SYNC", isPositive = true)
                    },
                    onClick = {
                        Toast.makeText(context, "Telephony sync observer is running actively", Toast.LENGTH_SHORT).show()
                    }
                )

                CardDivider()

                ModularSettingRow(
                    icon = Icons.Default.SimCard,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    title = "Mobile Carrier Network",
                    subtitle = carrierName,
                    trailing = {
                        StatusBadge(text = "SIM 1", isPositive = true)
                    },
                    onClick = {
                        Toast.makeText(context, "Connected via: $carrierName", Toast.LENGTH_SHORT).show()
                    }
                )

                CardDivider()

                ModularSettingRow(
                    icon = Icons.Default.Security,
                    iconBg = if (hasSmsPermission) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                    iconTint = if (hasSmsPermission) Color(0xFF059669) else Color(0xFFDC2626),
                    title = "Telephony Permissions",
                    subtitle = if (hasSmsPermission) "READ_SMS, SEND_SMS & RECEIVE_SMS active" else "SMS permission required to send/receive",
                    trailing = {
                        StatusBadge(
                            text = if (hasSmsPermission) "GRANTED" else "MISSING",
                            isPositive = hasSmsPermission
                        )
                    },
                    onClick = {
                        Toast.makeText(
                            context,
                            if (hasSmsPermission) "All permissions granted and functional" else "Please grant SMS permissions",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }

        // Modular Group 2: Notifications & Quick OTP Features
        item {
            SectionHeader(title = "OTP & NOTIFICATIONS", icon = Icons.Default.Notifications)

            Spacer(modifier = Modifier.height(8.dp))

            ModularCard {
                ModularSettingRow(
                    icon = Icons.Default.Notifications,
                    iconBg = Color(0xFFFAF5FF),
                    iconTint = Color(0xFF9333EA),
                    title = "Instant OTP Popup Alert",
                    subtitle = "Display immediate verification code banner on screen",
                    trailing = {
                        Switch(
                            checked = otpBannerEnabled,
                            onCheckedChange = { otpBannerEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = DeepOnyx,
                                uncheckedTrackColor = SurfaceBorderStrong
                            )
                        )
                    }
                )

                CardDivider()

                ModularSettingRow(
                    icon = Icons.Default.ContentCopy,
                    iconBg = Color(0xFFFFFBEB),
                    iconTint = Color(0xFFD97706),
                    title = "One-Tap Auto-Copy OTP",
                    subtitle = "Show prominent copy button inside verification message bubbles",
                    trailing = {
                        Switch(
                            checked = autoCopyOtpEnabled,
                            onCheckedChange = { autoCopyOtpEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = DeepOnyx,
                                uncheckedTrackColor = SurfaceBorderStrong
                            )
                        )
                    }
                )

                CardDivider()

                ModularSettingRow(
                    icon = Icons.Default.Vibration,
                    iconBg = Color(0xFFF1F5F9),
                    iconTint = Color(0xFF475569),
                    title = "Haptic Tactile Feedback",
                    subtitle = "Gentle vibration on message delivery and tab switching",
                    trailing = {
                        Switch(
                            checked = hapticFeedbackEnabled,
                            onCheckedChange = { hapticFeedbackEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = DeepOnyx,
                                uncheckedTrackColor = SurfaceBorderStrong
                            )
                        )
                    }
                )

                CardDivider()

                ModularSettingRow(
                    icon = Icons.Default.DeleteSweep,
                    iconBg = Color(0xFFFEF2F2),
                    iconTint = Color(0xFFDC2626),
                    title = "Auto-Purge Expired OTPs",
                    subtitle = "Delete one-time verification passwords older than 24 hours",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDC2626).copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .clickable { onPurgeExpiredOtps() }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Clean Now",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                )
            }
        }

        // Modular Group 3: Spam & Fraud Shield
        item {
            SectionHeader(title = "SPAM & FRAUD SHIELD", icon = Icons.Default.Shield)

            Spacer(modifier = Modifier.height(8.dp))

            ModularCard {
                ModularSettingRow(
                    icon = Icons.Default.Shield,
                    iconBg = Color(0xFFECFDF5),
                    iconTint = Color(0xFF059669),
                    title = "Heuristic Phishing Protection",
                    subtitle = "Detects predatory links, lottery fraud, and unverified senders",
                    trailing = {
                        StatusBadge(text = "ACTIVE", isPositive = true)
                    }
                )

                CardDivider()

                ModularSettingRow(
                    icon = Icons.Default.Block,
                    iconBg = Color(0xFFF1F5F9),
                    iconTint = Color(0xFF475569),
                    title = "Blacklisted Phone Numbers",
                    subtitle = if (blockedCount > 0) "$blockedCount numbers blocked from inbox" else "No blocked senders",
                    trailing = {
                        StatusBadge(text = if (blockedCount > 0) "$blockedCount BLOCKED" else "CLEAN", isPositive = blockedCount == 0)
                    }
                )
            }
        }

        // Modular Group 4: Design & Styling Tokens
        item {
            SectionHeader(title = "DESIGN & INTERFACE", icon = Icons.Default.Palette)

            Spacer(modifier = Modifier.height(8.dp))

            ModularCard {
                ModularSettingRow(
                    icon = Icons.Default.Palette,
                    iconBg = Color(0xFFF8FAFC),
                    iconTint = DeepOnyx,
                    title = "Porcelain Minimal Aesthetics",
                    subtitle = "High-contrast clean surfaces with deep slate typography",
                    trailing = {
                        StatusBadge(text = "ACTIVE", isPositive = true)
                    },
                    onClick = {
                        Toast.makeText(context, "Current theme: Porcelain White Minimal", Toast.LENGTH_SHORT).show()
                    }
                )

                CardDivider()

                ModularSettingRow(
                    icon = Icons.Default.Tune,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = AccentBlue,
                    title = "Unified Liquid Glass Dock",
                    subtitle = "Single cohesive bottom bar container with spring micro-interactions",
                    trailing = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = LightSlate
                        )
                    },
                    onClick = {
                        Toast.makeText(context, "Bottom dock is unified and responsive", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Modular Group 5: Storage & Database
        item {
            SectionHeader(title = "STORAGE & PERSISTENCE", icon = Icons.Default.Storage)

            Spacer(modifier = Modifier.height(8.dp))

            ModularCard {
                ModularSettingRow(
                    icon = Icons.Default.Storage,
                    iconBg = Color(0xFFF1F5F9),
                    iconTint = Color(0xFF334155),
                    title = "Local Room Database",
                    subtitle = "Persisting pinned threads, starred OTPs, and cached metadata",
                    trailing = {
                        StatusBadge(text = "OPTIMIZED", isPositive = true)
                    },
                    onClick = {
                        Toast.makeText(context, "Local Room database cache is in healthy state", Toast.LENGTH_SHORT).show()
                    }
                )

                CardDivider()

                ModularSettingRow(
                    icon = Icons.Default.FileDownload,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = AccentBlue,
                    title = "Export Conversations Backup",
                    subtitle = "Export inbox message archives to JSON file via Share Sheet",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentBlue.copy(alpha = 0.12f))
                                .border(1.dp, AccentBlue.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .clickable { onExportBackup() }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Export",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                        }
                    }
                )
            }
        }

        // Modular Group 5: About & Version Information
        item {
            SectionHeader(title = "ABOUT PULSE", icon = Icons.Default.Info)

            Spacer(modifier = Modifier.height(8.dp))

            ModularCard {
                ModularSettingRow(
                    icon = Icons.Default.Info,
                    iconBg = Color(0xFFF1F5F9),
                    iconTint = DeepOnyx,
                    title = "Pulse Messenger & Gateway",
                    subtitle = "Version 1.2 Enterprise Edition • Real-Time SMS & Zero-Carrier OTPs",
                    trailing = {
                        StatusBadge(text = "v1.2", isPositive = true)
                    },
                    onClick = {
                        Toast.makeText(context, "Pulse Messenger & Gateway v1.2", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MutedSlate,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MutedSlate,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun ModularCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x140F172A),
                spotColor = Color(0x1C0F172A)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(PureWhite)
            .border(1.2.dp, SurfaceBorderStrong, RoundedCornerShape(20.dp))
            .padding(vertical = 4.dp)
    ) {
        content()
    }
}

@Composable
private fun CardDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(SurfaceBorder)
    )
}

@Composable
private fun ModularSettingRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // High-contrast, colored icon container squircle
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg)
                .border(1.dp, iconTint.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = DeepOnyx,
                letterSpacing = (-0.2).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MutedSlate,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        trailing()
    }
}

@Composable
private fun StatusBadge(
    text: String,
    isPositive: Boolean = true
) {
    val bg = if (isPositive) LiveGreen.copy(alpha = 0.14f) else Color(0xFFEF4444).copy(alpha = 0.14f)
    val border = if (isPositive) LiveGreen.copy(alpha = 0.35f) else Color(0xFFEF4444).copy(alpha = 0.35f)
    val textTint = if (isPositive) LiveGreen else Color(0xFFEF4444)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = textTint,
            letterSpacing = 0.4.sp
        )
    }
}
