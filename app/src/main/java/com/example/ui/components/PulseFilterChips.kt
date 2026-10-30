package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryTab
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LiquidGlassTokens
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite

@Composable
fun PulseFilterChips(
    selectedCategory: CategoryTab,
    categoryCounts: Map<CategoryTab, Int> = emptyMap(),
    onCategorySelected: (CategoryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(CategoryTab.values()) { category ->
            val isSelected = category == selectedCategory
            val count = categoryCounts[category] ?: 0

            LiquidGlassFilterChip(
                category = category,
                count = count,
                isSelected = isSelected,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun LiquidGlassFilterChip(
    category: CategoryTab,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chip_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) DeepOnyx else PureWhite,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chip_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PureWhite else CharcoalDark,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chip_content"
    )

    val borderColor = if (isSelected) Color(0xFF334155) else Color(0xFFCBD5E1)

    val icon: ImageVector = when (category) {
        CategoryTab.ALL -> Icons.Default.Inbox
        CategoryTab.PERSONAL -> Icons.Default.Person
        CategoryTab.TRANSACTIONS -> Icons.Default.Shield
        CategoryTab.UPDATES -> Icons.Default.Campaign
        CategoryTab.ARCHIVED -> Icons.Default.Archive
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .height(38.dp)
            .shadow(
                elevation = if (isSelected) 4.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x180F172A),
                spotColor = Color(0x220F172A)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = 1.2.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = category.label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
                letterSpacing = (-0.1).sp
            )

            if (count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0x33FFFFFF) else Color(0x15000000)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}
