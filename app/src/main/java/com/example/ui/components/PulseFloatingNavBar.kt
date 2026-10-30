package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavigationTab
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LiquidGlassTokens
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite

/**
 * Unified Creamy Liquid Glass Navigation Dock:
 * Houses both navigation destinations and the quick compose action inside one unified,
 * seamless, beautifully rounded glass box container with spring micro-interactions.
 */
@Composable
fun PulseFloatingNavBar(
    selectedTab: NavigationTab,
    unreadMessagesTotal: Int = 0,
    onTabSelected: (NavigationTab) -> Unit,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // High-Contrast Glass Gradient & Specular Rim Light
    val navDockBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF8FAFC)
        )
    )

    val navDockBorder = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF94A3B8),
            Color(0xFFCBD5E1),
            Color(0xFFE2E8F0)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // The Single Unified High-Contrast Glass Box
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color(0x280F172A),
                    spotColor = Color(0x380F172A)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(navDockBackground)
                .border(
                    width = 1.5.dp,
                    brush = navDockBorder,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 7.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Navigation Tabs
            UnifiedGlassNavItem(
                tab = NavigationTab.INBOX,
                label = "Inbox",
                badgeCount = unreadMessagesTotal,
                isSelected = selectedTab == NavigationTab.INBOX,
                selectedIcon = Icons.Filled.ChatBubble,
                unselectedIcon = Icons.Filled.ChatBubbleOutline,
                onClick = { onTabSelected(NavigationTab.INBOX) }
            )

            UnifiedGlassNavItem(
                tab = NavigationTab.SAVED,
                label = "Saved",
                badgeCount = 0,
                isSelected = selectedTab == NavigationTab.SAVED,
                selectedIcon = Icons.Filled.Bookmark,
                unselectedIcon = Icons.Filled.BookmarkBorder,
                onClick = { onTabSelected(NavigationTab.SAVED) }
            )

            UnifiedGlassNavItem(
                tab = NavigationTab.SETTINGS,
                label = "Settings",
                badgeCount = 0,
                isSelected = selectedTab == NavigationTab.SETTINGS,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                onClick = { onTabSelected(NavigationTab.SETTINGS) }
            )

            // Delicate Glass Divider inside the unified box
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(Color(0xFFCBD5E1))
            )

            // Integrated Tactile New Message Action Button
            UnifiedGlassComposeButton(
                onClick = onComposeClick
            )
        }
    }
}

@Composable
private fun UnifiedGlassNavItem(
    tab: NavigationTab,
    label: String,
    badgeCount: Int,
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile press scale
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_item_press_scale"
    )

    // Playful icon bounce when selected
    var targetIconBounce by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            targetIconBounce = 1.25f
            kotlinx.coroutines.delay(120)
            targetIconBounce = 1f
        }
    }

    val iconBounceScale by animateFloatAsState(
        targetValue = targetIconBounce,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow),
        label = "icon_bounce"
    )

    // Infinite breathing animation for unread dot
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val badgePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_scale"
    )

    Box(
        modifier = Modifier
            .scale(buttonScale)
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (isSelected) DeepOnyx else Color(0xFFF1F5F9))
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF334155) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = if (isSelected) 13.dp else 10.dp, vertical = 8.dp)
            .testTag("nav_item_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier.scale(iconBounceScale),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                    contentDescription = label,
                    tint = if (isSelected) PureWhite else CharcoalDark,
                    modifier = Modifier.size(18.dp)
                )

                if (badgeCount > 0 && !isSelected) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .scale(badgePulse)
                            .clip(CircleShape)
                            .background(LiveGreen)
                    )
                }
            }

            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) PureWhite else CharcoalDark,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

@Composable
private fun UnifiedGlassComposeButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "compose_button_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isPressed) 90f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "compose_button_rotation"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF475569),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Message",
                tint = PureWhite,
                modifier = Modifier
                    .size(17.dp)
                    .rotate(rotation)
            )
            Text(
                text = "New",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                letterSpacing = (-0.2).sp
            )
        }
    }
}
