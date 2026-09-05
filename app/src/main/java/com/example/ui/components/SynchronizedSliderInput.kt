package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun SynchronizedSliderInput(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float,
    max: Float,
    unit: String,
    step: Float = 1f,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "slider_input"
) {
    val focusManager = LocalFocusManager.current
    var textInput by remember(value) {
        mutableStateOf(
            if (value % 1f == 0f) value.toInt().toString()
            else String.format(Locale.US, "%.1f", value)
        )
    }

    TactileCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shadowElevation = 1.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Label and Synchronized Numeric Input Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Range: ${min.toInt()} – ${max.toInt()} $unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.5.sp
                    )
                }

                // Sleek Integrated Numeric Badge Pill (Never clips, perfectly centered, responsive width)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                ) {
                    BasicTextField(
                        value = textInput,
                        onValueChange = { newStr ->
                            textInput = newStr
                            val parsed = newStr.toFloatOrNull()
                            if (parsed != null && parsed in min..max) {
                                onValueChange(parsed)
                            }
                        },
                        modifier = Modifier
                            .widthIn(min = 46.dp, max = 64.dp)
                            .padding(horizontal = 6.dp)
                            .testTag("${testTagPrefix}_field"),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val parsed = textInput.toFloatOrNull()
                                if (parsed != null) {
                                    val clamped = parsed.coerceIn(min, max)
                                    onValueChange(clamped)
                                    textInput = if (clamped % 1f == 0f) clamped.toInt().toString() else String.format(Locale.US, "%.1f", clamped)
                                } else {
                                    textInput = if (value % 1f == 0f) value.toInt().toString() else String.format(Locale.US, "%.1f", value)
                                }
                                focusManager.clearFocus()
                            }
                        )
                    )

                    // Vertical divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    )

                    // Integrated Unit Badge
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Tactile Slider
            Slider(
                value = value.coerceIn(min, max),
                onValueChange = { newVal ->
                    val stepped = (Math.round(newVal / step) * step).coerceIn(min, max)
                    onValueChange(stepped)
                    textInput = if (stepped % 1f == 0f) stepped.toInt().toString() else String.format(Locale.US, "%.1f", stepped)
                },
                valueRange = min..max,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${testTagPrefix}_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun CompactSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float,
    max: Float,
    unit: String,
    step: Float = 1f,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "slider"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Text(
                    text = "${if (value % 1f == 0f) value.toInt() else String.format(Locale.US, "%.1f", value)} $unit",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Slider(
            value = value.coerceIn(min, max),
            onValueChange = { newVal ->
                val stepped = (Math.round(newVal / step) * step).coerceIn(min, max)
                onValueChange(stepped)
            },
            valueRange = min..max,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("${testTagPrefix}_slider"),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
