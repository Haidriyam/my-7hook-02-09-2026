package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.JigProduct
import com.example.data.model.ProductConfiguration

enum class JigAngle(val label: String, val rotationZ: Float, val scaleX: Float) {
    SIDE_PROFILE("Side Profile", 0f, 1f),
    HERO_THREE_QUARTER("3/4 Hero", -4f, 1f),
    TOP_DORSAL("Top Dorsal", 0f, 0.92f),
    KEEL_VENTRAL("Keel Bottom", 4f, 0.95f)
}

/**
 * 7Hooks Product Preview
 * Clean, bounded product presentation showing the exact selected Jig photo.
 * Features:
 * - Immediate display of the real selected Jig product image (Orange-Black, Yellow-Dotted, etc.)
 * - Bounded height (approx 200dp) with generous breathing room
 * - Neutral studio background (Light: off-white, Dark: dark neutral)
 * - Interactive zoom controls (+ / - / reset) & gesture pinch
 * - Dynamic visual overlays for Front Ring, Back Ring, and Thread binding
 * - Multi-angle view selector
 */
@Composable
fun JigProductPreview(
    config: ProductConfiguration,
    selectedJig: JigProduct,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    var selectedAngleIndex by remember { mutableIntStateOf(0) }
    val angles = JigAngle.values()
    val currentAngle = angles[selectedAngleIndex]

    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = currentAngle.rotationZ,
        animationSpec = spring(),
        label = "rotation_anim"
    )

    // Neutral Studio Backgrounds (No decorative gradients or large blue voids)
    val studioBgColor = if (isDark) Color(0xFF131A26) else Color(0xFFF8FAFC)
    val studioBorderColor = if (isDark) Color(0xFF2A3649) else Color(0xFFE2E8F0)
    val floorShadowColor = if (isDark) Color(0x66000000) else Color(0x1F0F172A)

    val imageModel: Any = selectedJig.localDrawableRes ?: selectedJig.imageUrl

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("jig_product_preview_container")
    ) {
        // MAIN BOUNDED PRODUCT PREVIEW BOX
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(studioBgColor)
                .border(1.dp, studioBorderColor, RoundedCornerShape(10.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.85f, 2.2f)
                    }
                }
        ) {
            // 1. Subtle Floor Contact Shadow
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val w = size.width
                val h = size.height
                val shadowWidth = (w * 0.55f) * zoomScale.coerceIn(0.9f, 1.3f)
                val shadowHeight = 14.dp.toPx()
                val floorY = h * 0.82f

                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(floorShadowColor, Color.Transparent),
                        center = Offset(w / 2f, floorY),
                        radius = shadowWidth / 2f
                    ),
                    topLeft = Offset((w - shadowWidth) / 2f, floorY - shadowHeight / 2f),
                    size = Size(shadowWidth, shadowHeight)
                )
            }

            // 2. ACTUAL SELECTED PRODUCT IMAGE (Centered, Fit, with breathing room)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 22.dp)
                    .graphicsLayer {
                        scaleX = zoomScale * currentAngle.scaleX
                        scaleY = zoomScale
                        rotationZ = animatedRotation
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "${selectedJig.name} - ${selectedJig.modelNumber}",
                    contentScale = ContentScale.Fit,
                    error = painterResource(id = R.drawable.jig_orange_black_real),
                    fallback = painterResource(id = R.drawable.jig_orange_black_real),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("jig_actual_product_image")
                )
            }

            // 3. TOP BAR: Title Pill & Zoom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Preview Status Pill
                Surface(
                    color = if (isDark) Color(0xEE1E293B) else Color(0xEEFFFFFF),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Text(
                            text = "PRODUCT PREVIEW",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Compact Zoom / Reset Controls
                Surface(
                    color = if (isDark) Color(0xEE1E293B) else Color(0xEEFFFFFF),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        IconButton(
                            onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.85f) },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(12.dp))
                        }

                        Text(
                            text = "${(zoomScale * 100).toInt()}%",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        IconButton(
                            onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.2f) },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(12.dp))
                        }

                        if (zoomScale != 1.0f || selectedAngleIndex != 0) {
                            IconButton(
                                onClick = {
                                    zoomScale = 1.0f
                                    selectedAngleIndex = 0
                                },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = "Reset Zoom", modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            // 4. VISUAL HARDWARE OVERLAYS: FRONT RING & BACK RING (Requirement 16, 17, 18)
            // Front Ring Indicator Callout (Left / Line Tie)
            Surface(
                color = if (isDark) Color(0xDD0F172A) else Color(0xEEFFFFFF),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    0.7.dp,
                    if (config.frontRing == "None") Color.Gray.copy(alpha = 0.5f) else Color(0xFF0284C7)
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Split Ring Visual Dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .border(
                                1.5.dp,
                                if (config.frontRing == "None") Color.Gray else Color(0xFF0284C7),
                                CircleShape
                            )
                    )
                    Text(
                        text = "FRONT RING: ${config.frontRing.uppercase()}",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            // Back Ring Indicator Callout (Right / Stinger)
            Surface(
                color = if (isDark) Color(0xDD0F172A) else Color(0xEEFFFFFF),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(
                    0.7.dp,
                    if (config.backRing == "None") Color.Gray.copy(alpha = 0.5f) else Color(0xFF0284C7)
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "BACK RING: ${config.backRing.uppercase()}",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.3.sp
                    )
                    // Split Ring Visual Dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .border(
                                1.5.dp,
                                if (config.backRing == "None") Color.Gray else Color(0xFF0284C7),
                                CircleShape
                            )
                    )
                }
            }

            // Thread Binding Indicator (Center Bottom if active)
            if (config.threadColor != "None") {
                val threadColorVal = getThreadColorValue(config.threadColor)
                Surface(
                    color = if (isDark) Color(0xDD0F172A) else Color(0xEEFFFFFF),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(0.7.dp, threadColorVal),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(threadColorVal, CircleShape)
                        )
                        Text(
                            text = "THREAD: ${config.threadColor.uppercase()}",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // MULTI-ANGLE SELECTOR CHIPS (Compact & Professional)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            angles.forEachIndexed { index, angle ->
                val isSelected = selectedAngleIndex == index
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedAngleIndex = index },
                    label = {
                        Text(
                            text = angle.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        borderWidth = 0.8.dp
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }
    }
}

/**
 * Returns a displayable Compose Color for the configured Thread name
 */
fun getThreadColorValue(threadName: String): Color {
    return when (threadName.lowercase()) {
        "black" -> Color(0xFF1E293B)
        "white" -> Color(0xFFF1F5F9)
        "red" -> Color(0xFFDC2626)
        "orange" -> Color(0xFFEA580C)
        "yellow" -> Color(0xFFEAB308)
        "green" -> Color(0xFF16A34A)
        "blue" -> Color(0xFF2563EB)
        "pink" -> Color(0xFFEC4899)
        "purple" -> Color(0xFF9333EA)
        "gold" -> Color(0xFFD97706)
        "silver" -> Color(0xFF94A3B8)
        else -> Color(0xFF64748B)
    }
}
