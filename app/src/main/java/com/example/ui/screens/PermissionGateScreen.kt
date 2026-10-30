package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PulseButton
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LightSlate
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PorcelainWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorderHairline
import com.example.ui.theme.SurfaceSubtle

@Composable
fun PermissionGateScreen(
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PorcelainWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(PureWhite)
                    .border(1.dp, SurfaceBorderHairline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = null,
                    tint = DeepOnyx,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to Pulse",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = DeepOnyx,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A real-time SMS manager built for speed, privacy, and seamless messaging.",
                fontSize = 15.sp,
                color = MutedSlate,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Permission benefits cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PureWhite)
                    .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(16.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BenefitRow(
                    icon = Icons.Default.Message,
                    title = "Real-Time SMS Sync",
                    description = "Instant reception and transmission of carrier text messages."
                )
                BenefitRow(
                    icon = Icons.Default.Contacts,
                    title = "Contact Resolution",
                    description = "Displays names and avatars for your conversations."
                )
                BenefitRow(
                    icon = Icons.Default.Lock,
                    title = "Privacy First",
                    description = "All messages remain strictly local on your device."
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            PulseButton(
                text = "Enable Messaging",
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = true,
                testTag = "grant_permissions_button"
            )
        }
    }
}

@Composable
private fun BenefitRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceSubtle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DeepOnyx,
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepOnyx
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = LightSlate,
                lineHeight = 16.sp
            )
        }
    }
}
