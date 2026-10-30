package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StarredMessageEntity
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LightSlate
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PorcelainWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorderHairline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedMessagesScreen(
    starredMessages: List<StarredMessageEntity>,
    onOpenThread: (address: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PorcelainWhite)
    ) {
        if (starredMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PureWhite)
                            .border(1.dp, SurfaceBorderHairline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = LightSlate,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No saved messages",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepOnyx
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Star important SMS, OTPs or notes in any conversation to bookmark them here.",
                        fontSize = 13.sp,
                        color = MutedSlate,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
            ) {
                items(starredMessages, key = { it.messageId }) { item ->
                    val dateFormatted = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(item.date))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PureWhite)
                            .border(0.5.dp, SurfaceBorderHairline, RoundedCornerShape(16.dp))
                            .clickable { onOpenThread(item.address) }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            PulseAvatar(
                                initials = item.address.take(2).uppercase(),
                                seed = item.address,
                                size = 40.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.address,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DeepOnyx
                                    )
                                    Text(
                                        text = dateFormatted,
                                        fontSize = 11.sp,
                                        color = LightSlate
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = item.body,
                                    fontSize = 13.sp,
                                    color = MutedSlate,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
