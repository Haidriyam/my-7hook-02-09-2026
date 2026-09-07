package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ThreadOption(
    val name: String,
    val color: Color?,
    val isMetallic: Boolean = false,
    val isGlow: Boolean = false
)

val standardThreadOptions = listOf(
    ThreadOption("None", null),
    ThreadOption("Black", Color(0xFF1E293B)),
    ThreadOption("White", Color(0xFFF8FAFC)),
    ThreadOption("Red", Color(0xFFDC2626)),
    ThreadOption("Orange", Color(0xFFEA580C)),
    ThreadOption("Yellow", Color(0xFFEAB308)),
    ThreadOption("Green", Color(0xFF16A34A)),
    ThreadOption("Blue", Color(0xFF0284C7)),
    ThreadOption("Pink", Color(0xFFEC4899)),
    ThreadOption("Purple", Color(0xFF9333EA)),
    ThreadOption("Gold", Color(0xFFE2B024), isMetallic = true),
    ThreadOption("Silver", Color(0xFFCBD5E1), isMetallic = true),
    ThreadOption("Custom", Color(0xFF06B6D4))
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThreadSelector(
    selectedThread: String,
    onSelectThread: (ThreadOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedAdvanced by remember { mutableStateOf(false) }
    var selectedStyle by remember { mutableStateOf("Standard Nylon Thread") }
    var selectedLocation by remember { mutableStateOf("Assist Hook Shank") }

    TactileCard(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "THREAD",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Assist hook binding & whip finish: $selectedThread",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (selectedThread == "None") "NO THREAD" else selectedThread.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // FlowRow of realistic small wound thread/fiber visual samples
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                standardThreadOptions.forEach { thread ->
                    val isSelected = selectedThread.equals(thread.name, ignoreCase = true)
                    ThreadSampleItem(
                        thread = thread,
                        isSelected = isSelected,
                        onClick = { onSelectThread(thread) }
                    )
                }
            }

            // Optional "More Options" toggle
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { expandedAdvanced = !expandedAdvanced }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thread Wrapping & Fiber Specs",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (expandedAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expandedAdvanced) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Thread Fiber Formulation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val styles = listOf(
                        "Standard Nylon Thread",
                        "Fluorescent / UV Thread",
                        "Glow-in-the-Dark Thread",
                        "Metallic Tinsel Thread"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        styles.forEach { style ->
                            val active = selectedStyle == style
                            FilterChip(
                                selected = active,
                                onClick = { selectedStyle = style },
                                label = { Text(style, fontSize = 11.sp) },
                                leadingIcon = if (active) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Wrapping Location",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val locations = listOf(
                        "Assist Hook Shank",
                        "Assist Cord Binding",
                        "Eyelet Whip"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        locations.forEach { loc ->
                            val active = selectedLocation == loc
                            FilterChip(
                                selected = active,
                                onClick = { selectedLocation = loc },
                                label = { Text(loc, fontSize = 11.sp) },
                                leadingIcon = if (active) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreadSampleItem(
    thread: ThreadOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(52.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag("thread_item_${thread.name.lowercase()}")
    ) {
        // Wound spool / fiber sample segment
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(6.dp)
                )
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            if (thread.color == null) {
                // "None" option with diagonal slash
                Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, 0f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            } else {
                // Thread wound fiber swatch
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Background color
                    drawRect(
                        brush = if (thread.isMetallic) {
                            Brush.linearGradient(
                                colors = listOf(
                                    thread.color.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.8f),
                                    thread.color,
                                    thread.color.copy(alpha = 0.7f)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(w, h)
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    thread.color.copy(alpha = 0.8f),
                                    thread.color,
                                    thread.color.copy(alpha = 0.75f)
                                )
                            )
                        }
                    )

                    // Draw wound thread strands / texture
                    val strandCount = 6
                    val strandSpacing = h / strandCount
                    for (i in 0..strandCount) {
                        val y = i * strandSpacing
                        drawLine(
                            color = Color.Black.copy(alpha = 0.18f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.22f),
                            start = Offset(0f, y + 1.5f),
                            end = Offset(w, y + 1.5f),
                            strokeWidth = 0.8f
                        )
                    }

                    // Metallic sheen highlight
                    if (thread.isMetallic) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(w * 0.35f, 0f),
                            end = Offset(w * 0.35f, h),
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Subtle label under each thread swatch
        Text(
            text = thread.name,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
