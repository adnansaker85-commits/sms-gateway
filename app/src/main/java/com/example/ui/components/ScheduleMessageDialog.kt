package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.DeepOnyx
import com.example.ui.theme.LightSlate
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceBorderStrong
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleMessageDialog(
    recipientName: String,
    messageBody: String,
    onDismiss: () -> Unit,
    onScheduleConfirmed: (timestamp: Long) -> Unit
) {
    val now = System.currentTimeMillis()
    var selectedTimestamp by remember { mutableLongStateOf(now + 3600_000L) } // default +1 hour

    val options = listOf(
        SchedulePreset("In 30 Minutes", now + 30 * 60_000L),
        SchedulePreset("In 1 Hour", now + 3600_000L),
        SchedulePreset("In 3 Hours", now + 3 * 3600_000L),
        SchedulePreset("Tomorrow Morning (9 AM)", getTomorrowMorningTimestamp())
    )

    val dateFormat = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color(0x220F172A),
                    spotColor = Color(0x300F172A)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(PureWhite)
                .border(1.5.dp, SurfaceBorderStrong, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Schedule Message",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepOnyx
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = LightSlate,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Sending to $recipientName at scheduled time:",
                    fontSize = 13.sp,
                    color = MutedSlate
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Presets
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { preset ->
                        val isSelected = selectedTimestamp == preset.timestamp
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) DeepOnyx else Color(0xFFF8FAFC))
                                .border(
                                    1.2.dp,
                                    if (isSelected) DeepOnyx else SurfaceBorderStrong,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedTimestamp = preset.timestamp }
                                .padding(horizontal = 14.dp, vertical = 11.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = preset.label,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) PureWhite else CharcoalDark
                                )
                                Text(
                                    text = dateFormat.format(Date(preset.timestamp)),
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) Color(0xFFCBD5E1) else LightSlate
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Confirm button
                PulseButton(
                    text = "Schedule for ${dateFormat.format(Date(selectedTimestamp))}",
                    onClick = { onScheduleConfirmed(selectedTimestamp) },
                    icon = Icons.Default.AccessTime,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private data class SchedulePreset(val label: String, val timestamp: Long)

private fun getTomorrowMorningTimestamp(): Long {
    val cal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}
