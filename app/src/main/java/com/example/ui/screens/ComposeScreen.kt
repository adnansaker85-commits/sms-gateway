package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ContactItem
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LightSlate
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PorcelainWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorderHairline
import com.example.ui.theme.SurfaceSubtle

@Composable
fun ComposeScreen(
    contactSuggestions: List<ContactItem>,
    isSending: Boolean,
    onSearchContacts: (String) -> Unit,
    onBackClick: () -> Unit,
    onSendMessage: (recipient: String, text: String) -> Unit,
    onSelectContactToChat: (recipientNumber: String, recipientName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var recipient by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }

    BackHandler {
        onBackClick()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PorcelainWhite)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("compose_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = DeepOnyx
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "New Message",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepOnyx,
                letterSpacing = (-0.3).sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(SurfaceBorderHairline)
        )

        // Recipient Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "To:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MutedSlate
            )

            Spacer(modifier = Modifier.width(12.dp))

            BasicTextField(
                value = recipient,
                onValueChange = {
                    recipient = it
                    onSearchContacts(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("compose_recipient_input"),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = DeepOnyx,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(DeepOnyx),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                decorationBox = { innerTextField ->
                    if (recipient.isEmpty()) {
                        Text(
                            text = "Type name or phone number...",
                            fontSize = 14.sp,
                            color = LightSlate
                        )
                    }
                    innerTextField()
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(SurfaceBorderHairline)
        )

        // Contact Suggestions List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(contactSuggestions) { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PureWhite)
                        .border(0.5.dp, SurfaceBorderHairline, RoundedCornerShape(12.dp))
                        .clickable {
                            recipient = contact.phoneNumber
                            onSelectContactToChat(contact.phoneNumber, contact.name)
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulseAvatar(
                        initials = if (contact.name.isNotBlank()) contact.name.take(2).uppercase() else "#",
                        seed = contact.phoneNumber,
                        size = 38.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = contact.name.ifBlank { contact.phoneNumber },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepOnyx
                        )
                        Text(
                            text = contact.phoneNumber,
                            fontSize = 12.sp,
                            color = LightSlate
                        )
                    }
                }
            }
        }

        // Bottom Message Composer
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceSubtle)
                        .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(22.dp))
                        .padding(horizontal = 16.dp, vertical = 11.dp)
                ) {
                    BasicTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("compose_message_input"),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = DeepOnyx,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(DeepOnyx),
                        maxLines = 4,
                        decorationBox = { innerTextField ->
                            if (messageText.isEmpty()) {
                                Text(
                                    text = "Write your SMS message...",
                                    fontSize = 14.sp,
                                    color = LightSlate
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                val canSend = recipient.isNotBlank() && messageText.isNotBlank() && !isSending
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (canSend) DeepOnyx else SurfaceSubtle)
                        .clickable(enabled = canSend) {
                            onSendMessage(recipient.trim(), messageText.trim())
                        }
                        .testTag("compose_send_button"),
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
    }
}
