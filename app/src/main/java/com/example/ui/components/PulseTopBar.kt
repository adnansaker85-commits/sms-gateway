package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LightSlate
import com.example.ui.theme.LiquidGlassTokens
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorderHairline
import com.example.ui.theme.SurfaceSubtle

@Composable
fun PulseMainTopBar(
    title: String = "Pulse",
    isLiveSyncing: Boolean = true,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dot")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LiquidGlassTokens.GlassSurfaceUltra)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand & Live Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepOnyx,
                    letterSpacing = (-0.9).sp,
                    modifier = Modifier.testTag("app_brand_title")
                )

                // Liquid Glass Live Status Capsule
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(LiquidGlassTokens.GlassSurface)
                        .border(1.dp, LiquidGlassTokens.SpecularRimLight, RoundedCornerShape(16.dp))
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .scale(if (isLiveSyncing) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(LiveGreen)
                    )
                    Text(
                        text = "Live Sync",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedSlate,
                        letterSpacing = (-0.1).sp
                    )
                }
            }

            // Liquid Glass Search Trigger
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = LiquidGlassTokens.GlassShadowAmbient,
                        spotColor = LiquidGlassTokens.GlassShadowSpot
                    )
                    .clip(CircleShape)
                    .background(LiquidGlassTokens.GlassSurface)
                    .border(1.dp, LiquidGlassTokens.SpecularRimLight, CircleShape)
                    .clickable(onClick = onSearchClick)
                    .testTag("top_bar_search_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Messages",
                    tint = DeepOnyx,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Hairline specular divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(SurfaceBorderHairline)
        )
    }
}

@Composable
fun PulseChatTopBar(
    recipientName: String,
    phoneNumber: String,
    initials: String,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LiquidGlassTokens.GlassSurfaceUltra)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("chat_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DeepOnyx,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            PulseAvatar(
                initials = initials,
                seed = phoneNumber,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recipientName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepOnyx,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = phoneNumber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = LightSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Glass Call Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceSubtle)
                    .border(0.5.dp, SurfaceBorderHairline, CircleShape)
                    .clickable(onClick = onCallClick)
                    .testTag("chat_call_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call Contact",
                    tint = DeepOnyx,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("chat_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = DeepOnyx,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Hairline divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(SurfaceBorderHairline)
        )
    }
}
