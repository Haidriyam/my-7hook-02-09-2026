package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ThemeManager

enum class TactileButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINE,
    SUCCESS,
    DANGER
}

@Composable
fun TactileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TactileButtonVariant = TactileButtonVariant.PRIMARY,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    testTag: String? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    text: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile press animation (1.5dp downward shift, shadow compresses)
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed && enabled) 2.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "pressOffsetY"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (!enabled) 0.dp else if (isPressed) 1.dp else 4.dp,
        animationSpec = tween(durationMillis = 150),
        label = "shadowElevation"
    )

    // Palette per variant
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val (surfaceBrush, borderBrush, textColor, rimColor) = when (variant) {
        TactileButtonVariant.PRIMARY -> {
            val topColor = primaryColor.copy(alpha = 0.95f)
            val bottomColor = if (MaterialTheme.colorScheme.background == Color(0xFF0B1120)) Color(0xFF0369A1) else Color(0xFF0284C7)
            val shadowRim = Color(0xFF0369A1)
            Quad(
                Brush.verticalGradient(listOf(topColor, bottomColor)),
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)),
                Color.White,
                shadowRim
            )
        }
        TactileButtonVariant.SECONDARY -> {
            val topColor = secondaryColor.copy(alpha = 0.95f)
            val bottomColor = Color(0xFFC2410C)
            Quad(
                Brush.verticalGradient(listOf(topColor, bottomColor)),
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)),
                Color.White,
                Color(0xFF9A3412)
            )
        }
        TactileButtonVariant.OUTLINE -> {
            val bg = MaterialTheme.colorScheme.surface
            Quad(
                Brush.verticalGradient(listOf(bg, bg)),
                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outlineVariant)),
                MaterialTheme.colorScheme.onSurface,
                MaterialTheme.colorScheme.outline
            )
        }
        TactileButtonVariant.SUCCESS -> {
            Quad(
                Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)),
                Color.White,
                Color(0xFF047857)
            )
        }
        TactileButtonVariant.DANGER -> {
            Quad(
                Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))),
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)),
                Color.White,
                Color(0xFFB91C1C)
            )
        }
    }

    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .offset(y = pressOffsetY)
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(shape)
            .background(surfaceBrush)
            .border(
                BorderStroke(
                    1.dp,
                    if (variant == TactileButtonVariant.OUTLINE) SolidColor(MaterialTheme.colorScheme.outline) else borderBrush
                ),
                shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.2f)),
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(17.dp)
                        .padding(end = 6.dp)
                )
            }
            Text(
                text = text,
                color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun TactileCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    tonalElevation: Dp = 2.dp,
    shadowElevation: Dp = 3.dp,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Subtle top highlight line for 3D metallic feel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )
            content()
        }
    }
}

@Composable
fun TactileStepIndicator(
    currentStep: Int,
    totalSteps: Int = 3,
    stepTitles: List<String> = listOf("Choose Product", "Technical Specifications", "Engineering Document"),
    modifier: Modifier = Modifier
) {
    TactileCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP $currentStep OF $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = stepTitles.getOrElse(currentStep - 1) { "" },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..totalSteps) {
                    val isCompleted = i < currentStep
                    val isCurrent = i == currentStep
                    val activeProgress = if (isCompleted || isCurrent) 1f else 0f
                    val animatedProgress by animateFloatAsState(
                        targetValue = activeProgress,
                        animationSpec = tween(durationMillis = 300),
                        label = "stepProgress"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .background(
                                    if (isCurrent) {
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSelectorRow(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentMode = ThemeManager.currentThemeMode

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppThemeMode.values().forEach { mode ->
                val isSelected = currentMode == mode
                val bg by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    label = "themeBtnBg"
                )
                val tint by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "themeBtnTint"
                )

                Surface(
                    color = bg,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { ThemeManager.setThemeMode(context, mode) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                AppThemeMode.LIGHT -> Icons.Default.LightMode
                                AppThemeMode.DARK -> Icons.Default.DarkMode
                                AppThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                            },
                            contentDescription = mode.title,
                            tint = tint,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = mode.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
