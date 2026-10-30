package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SmsMessage
import com.example.ui.components.PulseChatTopBar
import com.example.ui.components.PulseMessageBubble
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LightSlate
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PorcelainWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorderHairline
import com.example.ui.theme.SurfaceSubtle

private val QuickReplies = listOf("Got it", "Thanks!", "On my way", "Call you in a bit", "Sounds good")

@Composable
fun ChatThreadScreen(
    threadId: Long,
    address: String,
    displayName: String,
    messages: List<SmsMessage>,
    isSending: Boolean,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onToggleStar: (SmsMessage) -> Unit,
    onScheduleMessage: (body: String, timestamp: Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var showScheduleDialog by remember { mutableStateOf(false) }

    BackHandler {
        onBackClick()
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val initials = remember(displayName) {
        val clean = displayName.trim()
        if (clean.isBlank()) "?"
        else {
            val parts = clean.split(" ").filter { it.isNotBlank() }
            if (parts.size >= 2) "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            else clean.take(2).uppercase()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PorcelainWhite)
    ) {
        // Bespoke Top Bar
        PulseChatTopBar(
            recipientName = displayName,
            phoneNumber = address,
            initials = initials,
            onBackClick = onBackClick,
            onCallClick = {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$address"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback
                }
            },
            onMenuClick = { /* Overflow options */ }
        )

        // Messages Timeline
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_messages_lazy_column"),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(
                items = messages,
                key = { it.id }
            ) { message ->
                PulseMessageBubble(
                    message = message,
                    onToggleStar = { onToggleStar(message) }
                )
            }
        }

        // Quick Reply Suggestions
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(QuickReplies) { reply ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PureWhite)
                        .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(16.dp))
                        .clickable { onSendMessage(reply) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = reply,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedSlate
                    )
                }
            }
        }

        // Tactile Composer Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite)
                .border(width = 0.5.dp, color = SurfaceBorderHairline)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Input Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceSubtle)
                        .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(22.dp))
                        .padding(horizontal = 16.dp, vertical = 11.dp)
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_input_text_field"),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = DeepOnyx,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(DeepOnyx),
                        maxLines = 4,
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Type an SMS message...",
                                    fontSize = 14.sp,
                                    color = LightSlate
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Character count / part indicator
                if (inputText.length > 100) {
                    val parts = (inputText.length / 160) + 1
                    Text(
                        text = "${inputText.length}/$parts",
                        fontSize = 10.sp,
                        color = LightSlate,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                // Schedule Message Button (Clock icon)
                if (inputText.isNotBlank() && !isSending) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF))
                            .border(1.dp, Color(0xFFBFDBFE), CircleShape)
                            .clickable { showScheduleDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Schedule SMS",
                            tint = com.example.ui.theme.AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Tactile Circular Send Button
                val canSend = inputText.isNotBlank() && !isSending
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (canSend) DeepOnyx else SurfaceSubtle)
                        .border(
                            1.dp,
                            if (canSend) DeepOnyx else SurfaceBorderHairline,
                            CircleShape
                        )
                        .clickable(enabled = canSend) {
                            val textToSend = inputText.trim()
                            if (textToSend.isNotBlank()) {
                                onSendMessage(textToSend)
                                inputText = ""
                            }
                        }
                        .testTag("chat_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = PureWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (canSend) PureWhite else LightSlate,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        if (showScheduleDialog) {
            com.example.ui.components.ScheduleMessageDialog(
                recipientName = displayName.ifBlank { address },
                messageBody = inputText.trim(),
                onDismiss = { showScheduleDialog = false },
                onScheduleConfirmed = { timestamp ->
                    showScheduleDialog = false
                    val textToSchedule = inputText.trim()
                    if (textToSchedule.isNotBlank()) {
                        onScheduleMessage(textToSchedule, timestamp)
                        inputText = ""
                    }
                }
            )
        }
    }
}
