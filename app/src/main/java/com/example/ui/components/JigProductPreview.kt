package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

enum class JigAngle(val label: String, val subtitle: String, val rotationZ: Float, val scaleX: Float, val scaleY: Float) {
    SIDE_PROFILE("Side Profile", "Lateral Keel 0°", 0f, 1f, 1f),
    HERO_THREE_QUARTER("3/4 Hero", "Isometric -4°", -4f, 1f, 0.96f),
    TOP_DORSAL("Top Dorsal", "Dorsal Spine 90° Tilt", 90f, 0.65f, 0.65f),
    KEEL_VENTRAL("Keel Bottom", "Ventral Belly 180° Tilt", 180f, 0.95f, 0.95f)
}

/**
 * 7Hooks Product Preview
 * Features:
 * - Real selected Jig product image on a light sky blue water background
 * - High-contrast dark badge chips with crisp light text in the four corners
 * - Interactive zoom controls (+ / - / reset) & gesture pinch
 * - Dynamic visual overlays for Front Ring, Back Ring, and Thread binding
 * - Polished multi-angle perspective selector with view indicators
 */
@Composable
fun JigProductPreview(
    config: ProductConfiguration,
    selectedJig: JigProduct,
    modifier: Modifier = Modifier
) {
    var selectedAngleIndex by remember { mutableIntStateOf(0) }
    val angles = JigAngle.values()
    val currentAngle = angles[selectedAngleIndex]

    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = currentAngle.rotationZ,
        animationSpec = spring(),
        label = "rotation_anim"
    )

    // Light Blue / Sky Blue Oceanic Water Background
    val waterGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE0F2FE), // Sky blue 100
            Color(0xFFBAE6FD), // Sky blue 200
            Color(0xFF7DD3FC)  // Sky blue 300
        )
    )
    val waterBorderColor = Color(0xFF38BDF8)
    val floorShadowColor = Color(0x330284C7)

    // High-Contrast Corner Badges (Dark background with light text)
    val cornerBadgeBg = Color(0xEE0F172A) // Dark slate navy
    val cornerBadgeBorder = Color(0xFF334155) // Slate 700 border
    val cornerBadgeText = Color(0xFFF8FAFC) // Crisp light white text

    val imageModel: Any = if (selectedJig.imageUrl.isNotBlank()) selectedJig.imageUrl else (selectedJig.localDrawableRes ?: R.drawable.ic_jig_icon)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("jig_product_preview_container")
    ) {
        // MAIN BOUNDED PRODUCT PREVIEW BOX (Light blue / sky blue ocean water background)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(waterGradient)
                .border(1.dp, waterBorderColor, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.85f, 2.2f)
                    }
                }
        ) {
            // 1. Water Surface Shimmer & Floor Depth Shadow
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val w = size.width
                val h = size.height

                // Subtle water caustics / wave lines
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(w * 0.1f, h * 0.25f),
                    end = Offset(w * 0.9f, h * 0.22f),
                    strokeWidth = 1.2.dp.toPx()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(w * 0.15f, h * 0.38f),
                    end = Offset(w * 0.85f, h * 0.40f),
                    strokeWidth = 0.8.dp.toPx()
                )

                // Depth Contact Shadow under the jig
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
                        scaleY = zoomScale * currentAngle.scaleY
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

            // 3. TOP BAR: Corner Badges with DARK BACKGROUND and LIGHT TEXT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TOP-LEFT CORNER: Product Preview Status Pill
                Surface(
                    color = cornerBadgeBg,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, cornerBadgeBorder),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Text(
                            text = "PRODUCT PREVIEW",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = cornerBadgeText,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // TOP-RIGHT CORNER: Compact Zoom / Reset Controls
                Surface(
                    color = cornerBadgeBg,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, cornerBadgeBorder),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        IconButton(
                            onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.85f) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Zoom Out",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        Text(
                            text = "${(zoomScale * 100).toInt()}%",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = cornerBadgeText,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        IconButton(
                            onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.2f) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Zoom In",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        if (zoomScale != 1.0f || selectedAngleIndex != 0) {
                            IconButton(
                                onClick = {
                                    zoomScale = 1.0f
                                    selectedAngleIndex = 0
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.RestartAlt,
                                    contentDescription = "Reset Zoom",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. BOTTOM CORNERS: HARDWARE OVERLAYS (Dark background with light text)
            // BOTTOM-LEFT CORNER: Front Ring Indicator Callout (Line Tie)
            val frontRingLabel = if (config.frontRing == "Custom" && config.customFrontRing.isNotEmpty()) {
                "FRONT: ${config.customFrontRing.uppercase()}"
            } else {
                "FRONT RING: ${config.frontRing.uppercase()}"
            }
            Surface(
                color = cornerBadgeBg,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(
                    0.8.dp,
                    if (config.frontRing == "None") Color.Gray.copy(alpha = 0.6f) else Color(0xFF38BDF8)
                ),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .border(
                                1.5.dp,
                                if (config.frontRing == "None") Color.Gray else Color(0xFF38BDF8),
                                CircleShape
                            )
                    )
                    Text(
                        text = frontRingLabel.take(20),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = cornerBadgeText,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            // BOTTOM-RIGHT CORNER: Back Ring Indicator Callout (Rear Stinger)
            val backRingLabel = if (config.backRing == "Custom" && config.customBackRing.isNotEmpty()) {
                "BACK: ${config.customBackRing.uppercase()}"
            } else {
                "BACK RING: ${config.backRing.uppercase()}"
            }
            Surface(
                color = cornerBadgeBg,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(
                    0.8.dp,
                    if (config.backRing == "None") Color.Gray.copy(alpha = 0.6f) else Color(0xFF38BDF8)
                ),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = backRingLabel.take(20),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = cornerBadgeText,
                        letterSpacing = 0.3.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .border(
                                1.5.dp,
                                if (config.backRing == "None") Color.Gray else Color(0xFF38BDF8),
                                CircleShape
                            )
                    )
                }
            }

            // BOTTOM-CENTER: Assist Cord Indicator (if active)
            if (config.threadColor != "None") {
                val threadColorVal = getThreadColorValue(config.threadColor)
                val cordLabel = if (config.threadColor == "Custom" && config.customAssistCordColor.isNotEmpty()) {
                    "CORD: ${config.customAssistCordColor.uppercase()}"
                } else {
                    "CORD: ${config.threadColor.uppercase()}"
                }
                Surface(
                    color = cornerBadgeBg,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, threadColorVal),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(threadColorVal, CircleShape)
                        )
                        Text(
                            text = cordLabel.take(18),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = cornerBadgeText,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // IMPROVED CAMERA PERSPECTIVE SELECTOR SECTION (Side profile, 3/4 hero, Top Dorsal, Keel Bottom)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "PERSPECTIVE VIEW",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = currentAngle.subtitle,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    angles.forEachIndexed { index, angle ->
                        val isSelected = selectedAngleIndex == index
                        Surface(
                            onClick = { selectedAngleIndex = index },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            shadowElevation = if (isSelected) 1.5.dp else 0.dp,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = angle.label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
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
