package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryTab
import com.example.model.SmsThread
import com.example.ui.components.PulseAvatar
import com.example.ui.components.PulseFilterChips
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LightSlate
import com.example.ui.theme.LiquidGlassTokens
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PorcelainWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorderHairline
import com.example.ui.theme.liquidGlass

@Composable
fun ConversationListScreen(
    threads: List<SmsThread>,
    selectedCategory: CategoryTab,
    categoryCounts: Map<CategoryTab, Int>,
    onCategorySelected: (CategoryTab) -> Unit,
    onThreadClick: (SmsThread) -> Unit,
    onTogglePin: (threadId: Long, isPinned: Boolean) -> Unit,
    onToggleArchive: (threadId: Long, isArchived: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PorcelainWhite)
    ) {
        // Unique Liquid Glass Category Filter Chips
        PulseFilterChips(
            selectedCategory = selectedCategory,
            categoryCounts = categoryCounts,
            onCategorySelected = onCategorySelected
        )

        // Threads List
        if (threads.isEmpty()) {
            EmptyInboxView(category = selectedCategory)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("conversation_lazy_list"),
                contentPadding = PaddingValues(bottom = 110.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = threads,
                    key = { it.threadId }
                ) { thread ->
                    ConversationItemRow(
                        thread = thread,
                        onClick = { onThreadClick(thread) },
                        onTogglePin = { onTogglePin(thread.threadId, !thread.isPinned) },
                        onToggleArchive = { onToggleArchive(thread.threadId, !thread.isArchived) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationItemRow(
    thread: SmsThread,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .liquidGlass(
                shape = RoundedCornerShape(20.dp),
                elevation = if (thread.unreadCount > 0) 8.dp else 4.dp,
                surfaceColor = if (thread.unreadCount > 0) PureWhite else LiquidGlassTokens.GlassSurfaceUltra
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .padding(horizontal = 16.dp, vertical = 13.dp)
            .testTag("thread_item_${thread.threadId}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            PulseAvatar(
                initials = thread.initials,
                seed = thread.address,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Contact and Snippet
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = thread.displayName,
                            fontSize = 15.sp,
                            fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                            color = DeepOnyx,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (thread.isPinned) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = AccentBlue,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = thread.formattedDate,
                        fontSize = 12.sp,
                        fontWeight = if (thread.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (thread.unreadCount > 0) DeepOnyx else LightSlate
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = thread.snippet.ifBlank { "No message preview" },
                        fontSize = 13.sp,
                        fontWeight = if (thread.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                        color = if (thread.unreadCount > 0) DeepOnyx else MutedSlate,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        lineHeight = 18.sp
                    )

                    if (thread.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(DeepOnyx)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = thread.unreadCount.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        }

        // Context Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (thread.isPinned) "Unpin Conversation" else "Pin to Top") },
                leadingIcon = {
                    Icon(
                        imageVector = if (thread.isPinned) Icons.Outlined.PushPin else Icons.Default.PushPin,
                        contentDescription = null
                    )
                },
                onClick = {
                    showMenu = false
                    onTogglePin()
                }
            )
            DropdownMenuItem(
                text = { Text(if (thread.isArchived) "Unarchive" else "Archive") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null
                    )
                },
                onClick = {
                    showMenu = false
                    onToggleArchive()
                }
            )
        }
    }
}

@Composable
private fun EmptyInboxView(
    category: CategoryTab
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .liquidGlass(
                        shape = CircleShape,
                        elevation = 8.dp,
                        surfaceColor = PureWhite
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = LightSlate,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "No messages in ${category.label}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DeepOnyx
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Messages will appear here in real-time as soon as they arrive on your phone.",
                fontSize = 13.sp,
                color = MutedSlate,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}
