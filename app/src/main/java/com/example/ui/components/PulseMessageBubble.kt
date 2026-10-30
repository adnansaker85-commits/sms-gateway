package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SmsMessage
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.BubbleReceived
import com.example.ui.theme.BubbleReceivedText
import com.example.ui.theme.BubbleSent
import com.example.ui.theme.BubbleSentText
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LightSlate
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorderHairline

@Composable
fun PulseMessageBubble(
    message: SmsMessage,
    onToggleStar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSent = message.isSent

    val bubbleShape = if (isSent) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }

    val bubbleBg = if (isSent) BubbleSent else BubbleReceived
    val textColor = if (isSent) BubbleSentText else BubbleReceivedText

    val starInteraction = remember { MutableInteractionSource() }
    val isStarPressed by starInteraction.collectIsPressedAsState()
    val starScale by animateFloatAsState(
        targetValue = if (isStarPressed) 1.35f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow),
        label = "star_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = if (isSent) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.widthIn(max = 290.dp),
            horizontalAlignment = if (isSent) Alignment.End else Alignment.Start
        ) {
            // Main Bubble Container
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .border(
                        width = if (isSent) 0.dp else 0.5.dp,
                        color = if (isSent) Color.Transparent else SurfaceBorderHairline,
                        shape = bubbleShape
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("message_bubble_${message.id}")
            ) {
                Column {
                    Text(
                        text = message.body,
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        letterSpacing = 0.1.sp
                    )

                    // Inline OTP Box if detected
                    message.extractedOtp?.let { otp ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSent) Color(0x22FFFFFF) else PureWhite)
                                .border(
                                    0.5.dp,
                                    if (isSent) Color(0x33FFFFFF) else SurfaceBorderHairline,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("OTP Code", otp))
                                    Toast.makeText(context, "Code $otp copied", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "VERIFICATION CODE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSent) Color(0xCCFFFFFF) else LightSlate,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = otp,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSent) PureWhite else DeepOnyx
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = if (isSent) PureWhite else AccentBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Copy",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSent) PureWhite else AccentBlue
                                )
                            }
                        }
                    }
                }
            }

            // Timestamp, Star & Delivery Status
            Row(
                modifier = Modifier.padding(top = 3.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = message.formattedTime,
                    fontSize = 11.sp,
                    color = LightSlate
                )

                if (isSent) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Delivered",
                        tint = AccentBlue,
                        modifier = Modifier.size(13.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .scale(starScale)
                        .size(18.dp)
                        .clickable(
                            interactionSource = starInteraction,
                            indication = null,
                            onClick = onToggleStar
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (message.isStarred) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (message.isStarred) "Starred" else "Star",
                        tint = if (message.isStarred) DeepOnyx else Color(0x6694A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
