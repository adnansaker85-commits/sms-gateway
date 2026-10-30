package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SmsThread
import com.example.ui.components.PulseAvatar
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LightSlate
import com.example.ui.theme.LiquidGlassTokens
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PorcelainWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorderHairline
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.liquidGlass

private val QuickSearchPills = listOf("Bank", "OTP", "Verification", "Google", "Amazon", "Code", "Delivery")

@Composable
fun PulseSearchScreen(
    searchQuery: String,
    threads: List<SmsThread>,
    onQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    onThreadClick: (SmsThread) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchScope by remember { mutableStateOf("All") }

    BackHandler {
        onCloseSearch()
    }

    val filteredThreads = remember(threads, searchQuery, searchScope) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            threads.filter {
                val matchName = it.displayName.lowercase().contains(q)
                val matchAddress = it.address.lowercase().contains(q)
                val matchBody = it.snippet.lowercase().contains(q)

                when (searchScope) {
                    "Contacts" -> matchName || matchAddress
                    "Messages" -> matchBody
                    "OTPs" -> matchBody && (it.snippet.contains("OTP", ignoreCase = true) || it.snippet.contains("code", ignoreCase = true))
                    else -> matchName || matchAddress || matchBody
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PorcelainWhite)
            .statusBarsPadding()
    ) {
        // Dedicated Liquid Glass Search Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .liquidGlass(
                    shape = RoundedCornerShape(26.dp),
                    elevation = 12.dp,
                    surfaceColor = LiquidGlassTokens.GlassSurfaceUltra
                )
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCloseSearch,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepOnyx,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MutedSlate,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dedicated_search_text_field"),
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = DeepOnyx,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(DeepOnyx),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search contacts, messages or OTPs...",
                                fontSize = 14.sp,
                                color = LightSlate
                            )
                        }
                        innerTextField()
                    }
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MutedSlate,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }

        // Scope Pills (All, Contacts, Messages, OTPs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Contacts", "Messages", "OTPs").forEach { scope ->
                val isSelected = searchScope == scope
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) DeepOnyx else PureWhite)
                        .border(
                            1.dp,
                            if (isSelected) DeepOnyx else SurfaceBorderHairline,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { searchScope = scope }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = scope,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) PureWhite else MutedSlate
                    )
                }
            }
        }

        // Search Content or Suggestions
        if (searchQuery.isBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = LightSlate,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Quick Search Filters",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightSlate,
                        letterSpacing = 0.6.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(QuickSearchPills) { pill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(PureWhite)
                                .border(1.dp, SurfaceBorderHairline, RoundedCornerShape(16.dp))
                                .clickable { onQueryChange(pill) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = pill,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = DeepOnyx
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SurfaceSubtle)
                                .border(1.dp, SurfaceBorderHairline, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = LightSlate,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Find Anything Instantly",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepOnyx
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Search across conversations, contact names, and SMS text bodies.",
                            fontSize = 13.sp,
                            color = MutedSlate,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 30.dp)
                        )
                    }
                }
            }
        } else {
            // Results list with highlighted matches
            if (filteredThreads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "No results found for \"$searchQuery\"",
                        fontSize = 14.sp,
                        color = MutedSlate
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "${filteredThreads.size} RESULTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightSlate,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                        )
                    }

                    items(filteredThreads, key = { it.threadId }) { thread ->
                        SearchResultRow(
                            thread = thread,
                            query = searchQuery,
                            onClick = { onThreadClick(thread) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    thread: SmsThread,
    query: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(18.dp),
                elevation = 6.dp,
                surfaceColor = PureWhite
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PulseAvatar(
                initials = thread.initials,
                seed = thread.address,
                size = 42.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = thread.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepOnyx,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = thread.formattedDate,
                        fontSize = 11.sp,
                        color = LightSlate
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Highlighted matching text snippet
                val highlightedSnippet = remember(thread.snippet, query) {
                    buildHighlightedString(thread.snippet, query)
                }

                Text(
                    text = highlightedSnippet,
                    fontSize = 13.sp,
                    color = MutedSlate,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun buildHighlightedString(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    if (query.isBlank() || !text.contains(query, ignoreCase = true)) {
        return androidx.compose.ui.text.AnnotatedString(text)
    }

    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var startIndex = 0

        while (startIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (matchIndex == -1) {
                append(text.substring(startIndex))
                break
            }

            if (matchIndex > startIndex) {
                append(text.substring(startIndex, matchIndex))
            }

            withStyle(
                style = SpanStyle(
                    color = DeepOnyx,
                    fontWeight = FontWeight.Bold,
                    background = Color(0x332563EB)
                )
            ) {
                append(text.substring(matchIndex, matchIndex + query.length))
            }

            startIndex = matchIndex + query.length
        }
    }
}
