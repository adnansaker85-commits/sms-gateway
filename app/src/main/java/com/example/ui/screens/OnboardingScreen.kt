package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch

data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val tag: String,
    val icon: ImageVector,
    val accentColor: Color,
    val previewType: Int // 1: Welcome, 2: Categorization, 3: OTP Notification, 4: Sync & Permissions
)

@Composable
fun OnboardingScreen(
    hasSmsPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onRequestSmsPermissions: () -> Unit,
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = listOf(
        OnboardingSlide(
            title = "Welcome to Pulse",
            subtitle = "A bespoke, porcelain-white SMS manager engineered for privacy, speed, and distraction-free communication.",
            tag = "BESPOKE MESSAGING",
            icon = Icons.Default.AutoAwesome,
            accentColor = DeepOnyx,
            previewType = 1
        ),
        OnboardingSlide(
            title = "Smart Auto-Categorization",
            subtitle = "Your incoming texts are automatically classified into Personal chats, Banking transactions, OTP codes, and Service updates.",
            tag = "ORGANIZED INBOX",
            icon = Icons.Default.Category,
            accentColor = AccentBlue,
            previewType = 2
        ),
        OnboardingSlide(
            title = "Instant OTP Alerts & Copy",
            subtitle = "Never dig for verification codes again. Pulse auto-detects OTPs and offers one-tap clipboard copying directly from your notifications.",
            tag = "ZERO-FRICTION OTP",
            icon = Icons.Default.Key,
            accentColor = LiveGreen,
            previewType = 3
        ),
        OnboardingSlide(
            title = "Real-Time Telephony Sync",
            subtitle = "Directly connected to native Android Telephony with real-time background sync, instant search, and zero carrier fees.",
            tag = "READY TO START",
            icon = Icons.Default.Sync,
            accentColor = Color(0xFF7C3AED),
            previewType = 4
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastSlide = pagerState.currentPage == slides.size - 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PorcelainWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: Brand label & Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LiveGreen)
                )
                Text(
                    text = "PULSE MESSENGER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightSlate,
                    letterSpacing = 1.sp
                )
            }

            if (!isLastSlide) {
                Text(
                    text = "Skip",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedSlate,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(slides.size - 1)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pager Carousel
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            val slide = slides[page]
            OnboardingSlideView(
                slide = slide,
                hasSmsPermission = hasSmsPermission,
                hasNotificationPermission = hasNotificationPermission,
                onRequestNotificationPermission = onRequestNotificationPermission,
                onRequestSmsPermissions = onRequestSmsPermissions
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom Controls: Animated Pill Pager Indicator + Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pager Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(slides.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "pager_dot_width"
                    )

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) DeepOnyx else Color(0xFFCBD5E1))
                    )
                }
            }

            // Next / Finish Button
            if (isLastSlide) {
                PulseButton(
                    text = "Get Started",
                    onClick = {
                        if (!hasSmsPermission) {
                            onRequestSmsPermissions()
                        }
                        onCompleteOnboarding()
                    },
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.testTag("onboarding_finish_button")
                )
            } else {
                PulseButton(
                    text = "Next",
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.testTag("onboarding_next_button")
                )
            }
        }
    }
}

@Composable
private fun OnboardingSlideView(
    slide: OnboardingSlide,
    hasSmsPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onRequestSmsPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tag badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(PureWhite)
                .border(1.dp, SurfaceBorderStrong, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = slide.tag,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = slide.accentColor,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Visual Card
        Box(
            modifier = Modifier
                .size(160.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color(0x180F172A),
                    spotColor = Color(0x240F172A)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            PureWhite,
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .border(1.5.dp, SurfaceBorderStrong, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(slide.accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = null,
                    tint = slide.accentColor,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Title & Description
        Text(
            text = slide.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DeepOnyx,
            letterSpacing = (-0.6).sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = slide.subtitle,
            fontSize = 14.sp,
            color = MutedSlate,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Slide-Specific Dynamic Interactive Component
        when (slide.previewType) {
            2 -> {
                // Category Pills Preview
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryChipPreview("Personal", DeepOnyx, PureWhite)
                    CategoryChipPreview("Transactions", PureWhite, CharcoalDark)
                    CategoryChipPreview("OTPs", PureWhite, CharcoalDark)
                    CategoryChipPreview("Updates", PureWhite, CharcoalDark)
                }
            }

            3 -> {
                // OTP Notification Mock Card & Permission Trigger
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PureWhite)
                            .border(1.2.dp, SurfaceBorderStrong, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Bank Alert • OTP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightSlate
                                )
                                Text(
                                    text = "Your verification code is 849201",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepOnyx
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LiveGreen.copy(alpha = 0.15f))
                                    .border(1.dp, LiveGreen.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "📋 Copy Code",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiveGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!hasNotificationPermission) {
                        Text(
                            text = "🔔 Tap to Allow Notification Alerts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onRequestNotificationPermission)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = LiveGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Notification alerts enabled",
                                fontSize = 12.sp,
                                color = LiveGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            4 -> {
                // SMS Permission Prompt Card
                if (!hasSmsPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEFF6FF))
                            .border(1.2.dp, Color(0xFFBFDBFE), RoundedCornerShape(16.dp))
                            .clickable(onClick = onRequestSmsPermissions)
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Tap to Grant SMS & Contacts Access",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue
                                )
                                Text(
                                    text = "Required for sending, receiving & syncing messages",
                                    fontSize = 11.sp,
                                    color = MutedSlate
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LiveGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Telephony permissions granted",
                            fontSize = 13.sp,
                            color = LiveGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipPreview(name: String, bg: Color, textTint: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, SurfaceBorderStrong, RoundedCornerShape(12.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textTint
        )
    }
}
