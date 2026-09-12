package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.geometry.JigGeometryEngine.JigSilhouetteType
import com.example.data.model.JigConfiguration
import com.example.data.model.JigShapeRepository
import com.example.data.model.JigShapeTemplate
import kotlin.math.cos
import kotlin.math.sin

enum class JigPreviewPerspective(
    val label: String,
    val rotationZ: Float,
    val scaleY: Float,
    val subtitle: String
) {
    SIDE_PROFILE("Side Profile", 0f, 1.0f, "Lateral Keel 0°"),
    HERO_THREE_QUARTER("3/4 Hero", -4f, 0.94f, "Studio Angle -4°"),
    TOP_DORSAL("Top Dorsal", 90f, 0.60f, "Dorsal Spine 90°"),
    KEEL_VENTRAL("Keel Belly", 180f, 0.95f, "Ventral Hydro-Keel 180°")
}

/**
 * Deterministic local live-preview compositor for 7Hooks Jigs.
 * Renders layered CAD geometry, dual-tone body color, pattern masks,
 * material surface shaders, 3D eyes, forged assist hooks, braided cord,
 * and solid welded rings with immediate 60fps responsiveness.
 */
@Composable
fun JigLiveCanvasPreview(
    config: JigConfiguration,
    modifier: Modifier = Modifier,
    activeStep: Int = 10, // Allows progressive layer reveal during configuration
    showControls: Boolean = true,
    onCaptureReady: ((Bitmap) -> Unit)? = null
) {
    val shapeTemplate = remember(config.shapeId) {
        JigShapeRepository.getById(config.shapeId)
    }

    var selectedPerspective by remember { mutableStateOf(JigPreviewPerspective.HERO_THREE_QUARTER) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val animatedRotation by animateFloatAsState(
        targetValue = selectedPerspective.rotationZ,
        animationSpec = spring(),
        label = "rotation_anim"
    )

    // Studio Clean Oceanic Background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF1F5F9), // Slate 100
            Color(0xFFE2E8F0), // Slate 200
            Color(0xFFCBD5E1)  // Slate 300
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("jig_live_preview_container")
    ) {
        // MAIN LIVE CANVAS BOX
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundGradient)
                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.7f, 2.5f)
                        panOffset = Offset(
                            x = (panOffset.x + pan.x).coerceIn(-180f, 180f),
                            y = (panOffset.y + pan.y).coerceIn(-120f, 120f)
                        )
                    }
                }
        ) {
            // LAYER 0: Studio Floor Shadow & Grid
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f + panOffset.x
                val cy = size.height / 2f + panOffset.y + 45f

                // Subtle studio contact shadow
                drawOval(
                    color = Color(0x280F172A),
                    topLeft = Offset(cx - size.width * 0.38f * zoomScale, cy),
                    size = Size(size.width * 0.76f * zoomScale, 24f * zoomScale)
                )
            }

            // LAYER 1-10: THE PARAMETRIC JIG COMPOSITOR
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = zoomScale
                        scaleY = zoomScale * selectedPerspective.scaleY
                        rotationZ = animatedRotation
                        translationX = panOffset.x
                        translationY = panOffset.y
                    }
                    .testTag("jig_composited_canvas")
            ) {
                renderJigCompositor(
                    config = config,
                    template = shapeTemplate,
                    activeStep = activeStep,
                    perspective = selectedPerspective
                )
            }

            // CORNER BADGES: Crisp Industrial Metadata
            // Top-Left: Shape & Profile
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xDD0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(
                        text = shapeTemplate.shapeName.uppercase(),
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${config.weightGrams.toInt()}g • ${config.lengthMm.toInt()}mm",
                        color = Color(0xFFE2E8F0),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Top-Right: Finish & Pattern
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xDD0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Text(
                    text = "${config.finish.uppercase()} / ${config.pattern.uppercase()}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    color = Color(0xFFF1F5F9),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Bottom-Left: Live Hardware Status
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xCC0F172A)
            ) {
                val hookLabel = if (config.assistHook == "None") "NO HOOK" else "ASSIST 3/0"
                val ringLabel = if (config.frontRing == "None") "NO RING" else "SOLID RING"
                Text(
                    text = "$hookLabel • $ringLabel",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Bottom-Right: Interactive Zoom / Pan Controls
            if (showControls) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.5f) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xDD0F172A), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.7f) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xDD0F172A), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            zoomScale = 1.0f
                            panOffset = Offset.Zero
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xDD0F172A), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Zoom",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // PERSPECTIVE SWITCHER TABS
        if (showControls) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JigPreviewPerspective.values().forEach { perspective ->
                    val isSelected = selectedPerspective == perspective
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPerspective = perspective },
                        label = {
                            Text(
                                text = perspective.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0E3A68),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF1F5F9),
                            labelColor = Color(0xFF334155)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Internal Drawing Compositor implementing the multi-layer pipeline:
 * Geometry -> Base Color / Dual-Tone -> Patterns (Clipped) -> Finishes -> 3D Eyes -> Hooks & Cords -> Rings
 * Fully deterministic, immediate local preview with zero artificial step hiding.
 */
private fun DrawScope.renderJigCompositor(
    config: JigConfiguration,
    template: JigShapeTemplate,
    activeStep: Int,
    perspective: JigPreviewPerspective
) {
    val cx = size.width / 2f
    val cy = size.height / 2f

    // Scale body dimensions to viewport
    val baseLengthPx = size.width * 0.72f
    val lengthScale = (config.lengthMm / 130f).coerceIn(0.85f, 1.25f)
    val widthScale = (config.widthMm / 22f).coerceIn(0.80f, 1.30f)

    val bodyLength = baseLengthPx * lengthScale
    val bodyWidth = (bodyLength / template.aspectRatio) * widthScale
    val halfL = bodyLength / 2f
    val halfW = bodyWidth / 2f

    // 1. BUILD SILHOUETTE PATH
    val bodyPath = buildSilhouettePath(template.silhouetteType, cx, cy, halfL, halfW)

    // 2. BASE COLOR LAYER (Progressive: CAD raw blank at Steps 1-3, painted at Step 4+)
    if (activeStep < 4) {
        // Raw machined titanium/alloy CAD blank
        val blankBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE2E8F0),
                Color(0xFFCBD5E1),
                Color(0xFF94A3B8),
                Color(0xFFE2E8F0)
            ),
            start = Offset(cx - halfL, cy - halfW),
            end = Offset(cx + halfL, cy + halfW)
        )
        drawPath(path = bodyPath, brush = blankBrush)

        // Technical CAD measurement overlay grid
        clipPath(bodyPath) {
            val stepX = halfL / 5f
            for (i in -4..4) {
                val gx = cx + i * stepX
                drawLine(
                    color = Color(0x33475569),
                    start = Offset(gx, cy - halfW),
                    end = Offset(gx, cy + halfW),
                    strokeWidth = 1f
                )
            }
            drawLine(
                color = Color(0x550284C7),
                start = Offset(cx - halfL, cy),
                end = Offset(cx + halfL, cy),
                strokeWidth = 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
            )
        }
    } else {
        // Step 4+: Full chosen colors and dual-tone gradients
        val mainColor = Color(config.mainColorHex)
        val secondaryColor = Color(config.secondaryColorHex)

        if (config.hasDualTone) {
            val bodyBrush = Brush.verticalGradient(
                colors = listOf(
                    mainColor,
                    mainColor.copy(alpha = 0.90f),
                    secondaryColor
                ),
                startY = cy - halfW,
                endY = cy + halfW
            )
            drawPath(path = bodyPath, brush = bodyBrush)
        } else {
            drawPath(path = bodyPath, color = mainColor)
        }
    }

    // 3. PATTERN LAYER (Strictly clipped to jig surface, appears Step 5+)
    if (activeStep >= 5 && config.pattern.isNotBlank() && config.pattern != "Solid" && config.pattern != "None") {
        clipPath(bodyPath) {
            drawPatternOverlay(
                pattern = config.pattern,
                patternColor = Color(config.patternColorHex),
                cx = cx,
                cy = cy,
                halfL = halfL,
                halfW = halfW,
                bodyPath = bodyPath
            )
        }
    }

    // 4. MATERIAL & SURFACE FINISH LAYER (Appears Step 6+)
    if (activeStep >= 6 && config.finish.isNotBlank() && config.finish != "None") {
        clipPath(bodyPath) {
            drawFinishShader(
                finish = config.finish,
                cx = cx,
                cy = cy,
                halfL = halfL,
                halfW = halfW
            )
        }
    }

    // Body Contour Border / Edge Definition
    drawPath(
        path = bodyPath,
        color = if (activeStep < 4) Color(0xFF475569) else Color(0x660F172A),
        style = Stroke(width = if (activeStep < 4) 2.0f else 1.5f)
    )

    // 5. 3D EYE LAYER (Appears Step 7+, dynamic eye size scaling)
    if (activeStep >= 7 && config.eyeStyle != "None") {
        val eyeX = cx - halfL + (bodyLength * template.eyeAnchorX)
        val eyeY = cy - halfW + (bodyWidth * template.eyeAnchorY)
        val eyeSizeMultiplier = when {
            config.eyeSize.contains("Small", ignoreCase = true) -> 0.15f
            config.eyeSize.contains("Large", ignoreCase = true) -> 0.28f
            config.eyeSize.contains("Magnum", ignoreCase = true) -> 0.36f
            else -> 0.22f // Medium (8mm)
        }
        val eyeRadius = (bodyWidth * eyeSizeMultiplier).coerceIn(4f, 22f)

        draw3DEye(
            eyeX = eyeX,
            eyeY = eyeY,
            radius = eyeRadius,
            eyeStyle = config.eyeStyle,
            eyeColorName = config.eyeColor
        )
    }

    // 6. ASSIST HOOK & ASSIST CORD LAYER (Appears Step 8+, cord at Step 9+)
    if (activeStep >= 8 && config.hookType != "None" && config.assistHook != "None") {
        val anchorX = cx - halfL + (bodyLength * template.hookAnchorX)
        val anchorY = cy - halfW + (bodyWidth * template.hookAnchorY)
        val cordColor = resolveCordColor(config.assistCordColor)

        drawAssistHookAndCord(
            anchorX = anchorX,
            anchorY = anchorY,
            hookType = config.hookType.ifBlank { config.assistHook },
            hookSize = config.hookSize,
            cordColor = cordColor,
            showCord = activeStep >= 9 && config.assistCord != "None"
        )
    }

    // 7. SOLID RINGS LAYER (Appears Step 10+, with Front, Back, Top, Bottom rings & accurate sizing)
    if (activeStep >= 10) {
        val ringRadius = when {
            config.ringSize.contains("#4") -> 7.0f
            config.ringSize.contains("#6") -> 10.5f
            config.ringSize.contains("#7") -> 12.5f
            config.ringSize.contains("#8") -> 14.5f
            else -> 9.0f // #5 (5.5mm)
        }
        val isHeavy = config.frontRing.contains("Heavy", ignoreCase = true) ||
                config.backRing.contains("Heavy", ignoreCase = true) ||
                config.ringSize.contains("#7") || config.ringSize.contains("#8")
        val strokeW = if (isHeavy) 3.2f else 2.2f

        // 7a. FRONT LINE TIE NOSE RING (Firmly positioned outside nose tip with forged eyelet lug)
        if (config.frontRing != "None") {
            val noseTipX = cx - halfL
            val noseTipY = cy
            val ringCenter = Offset(noseTipX - ringRadius * 0.85f, noseTipY)

            // Forged metal attachment lug
            drawCircle(
                color = Color(0xFF64748B),
                radius = ringRadius * 0.40f,
                center = Offset(noseTipX + 1f, noseTipY)
            )
            // Solid Stainless Ring
            drawCircle(
                color = Color(0xFFCBD5E1),
                radius = ringRadius,
                center = ringCenter,
                style = Stroke(width = strokeW)
            )
            drawCircle(
                color = Color(0xFF475569),
                radius = ringRadius - strokeW / 2f,
                center = ringCenter,
                style = Stroke(width = 0.8f)
            )
        }

        // 7b. REAR TAIL SPLIT RING (Firmly positioned outside tail tip with forged eyelet lug)
        if (config.backRing != "None") {
            val tailTipX = cx + halfL
            val tailTipY = cy
            val ringCenter = Offset(tailTipX + ringRadius * 0.85f, tailTipY)

            // Forged metal attachment lug
            drawCircle(
                color = Color(0xFF64748B),
                radius = ringRadius * 0.40f,
                center = Offset(tailTipX - 1f, tailTipY)
            )
            // Split Ring
            drawCircle(
                color = Color(0xFFCBD5E1),
                radius = ringRadius,
                center = ringCenter,
                style = Stroke(width = strokeW)
            )
            drawCircle(
                color = Color(0xFF475569),
                radius = ringRadius - strokeW / 2f,
                center = ringCenter,
                style = Stroke(width = 0.8f)
            )
        }

        // 7c. TOP DORSAL RING (Dorsal balance ring for slow pitch jigs)
        if (config.topRing != "None") {
            val topX = cx
            val topY = cy - halfW
            val topRadius = ringRadius * 0.85f
            val ringCenter = Offset(topX, topY - topRadius * 0.85f)

            drawCircle(
                color = Color(0xFF64748B),
                radius = topRadius * 0.40f,
                center = Offset(topX, topY + 1f)
            )
            drawCircle(
                color = Color(0xFFCBD5E1),
                radius = topRadius,
                center = ringCenter,
                style = Stroke(width = strokeW)
            )
            drawCircle(
                color = Color(0xFF475569),
                radius = topRadius - strokeW / 2f,
                center = ringCenter,
                style = Stroke(width = 0.8f)
            )
        }

        // 7d. BOTTOM VENTRAL RING (Ventral belly ring for stinger assist hooks)
        if (config.bottomRing != "None") {
            val botX = cx
            val botY = cy + halfW
            val botRadius = ringRadius * 0.85f
            val ringCenter = Offset(botX, botY + botRadius * 0.85f)

            drawCircle(
                color = Color(0xFF64748B),
                radius = botRadius * 0.40f,
                center = Offset(botX, botY - 1f)
            )
            drawCircle(
                color = Color(0xFFCBD5E1),
                radius = botRadius,
                center = ringCenter,
                style = Stroke(width = strokeW)
            )
            drawCircle(
                color = Color(0xFF475569),
                radius = botRadius - strokeW / 2f,
                center = ringCenter,
                style = Stroke(width = 0.8f)
            )
        }
    }
}

/**
 * Builds the accurate 2D silhouette Path for the requested Shape.
 */
private fun buildSilhouettePath(
    type: JigSilhouetteType,
    cx: Float,
    cy: Float,
    halfL: Float,
    halfW: Float
): Path {
    return com.example.data.geometry.JigGeometryEngine.buildFrontSilhouettePath(type, cx, cy, halfL, halfW).asComposePath()
}

/**
 * Renders tactical patterns: Tiger, Dots, Circles, Triangles, H-Lines, V-Lines, Chevron, Diamonds, Scales, Natural Scale, Zebra
 */
private fun DrawScope.drawPatternOverlay(
    pattern: String,
    patternColor: Color,
    cx: Float,
    cy: Float,
    halfL: Float,
    halfW: Float,
    bodyPath: Path
) {
    val normPattern = pattern.lowercase().trim()
    when {
        normPattern.contains("tiger") -> {
            val stripeCount = 7
            val stepX = (halfL * 1.6f) / (stripeCount + 1)
            for (i in 1..stripeCount) {
                val sx = (cx - halfL * 0.8f) + i * stepX
                val stripePath = Path().apply {
                    moveTo(sx - 3.5f, cy - halfW * 0.9f)
                    lineTo(sx + 3.5f, cy - halfW * 0.9f)
                    lineTo(sx + 8f, cy + halfW * 0.9f)
                    lineTo(sx + 2f, cy + halfW * 0.9f)
                    close()
                }
                drawPath(path = stripePath, color = patternColor.copy(alpha = 0.88f))
            }
        }

        normPattern.contains("dots") -> {
            val dotCount = 10
            val stepX = (halfL * 1.5f) / (dotCount + 1)
            for (i in 1..dotCount) {
                val dx = (cx - halfL * 0.75f) + i * stepX
                val dy = cy + (if (i % 2 == 0) -halfW * 0.25f else halfW * 0.25f)
                drawCircle(color = patternColor, radius = 4f, center = Offset(dx, dy))
                drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 1.4f, center = Offset(dx - 1.2f, dy - 1.2f))
            }
        }

        normPattern.contains("circles") -> {
            val count = 5
            val stepX = (halfL * 1.5f) / (count + 1)
            for (i in 1..count) {
                val dx = (cx - halfL * 0.75f) + i * stepX
                drawCircle(color = patternColor.copy(alpha = 0.85f), radius = 7f, center = Offset(dx, cy), style = Stroke(width = 2.2f))
                drawCircle(color = patternColor.copy(alpha = 0.6f), radius = 3.5f, center = Offset(dx, cy), style = Stroke(width = 1.2f))
            }
        }

        normPattern.contains("triangles") -> {
            val count = 6
            val stepX = (halfL * 1.5f) / (count + 1)
            for (i in 1..count) {
                val tx = (cx - halfL * 0.75f) + i * stepX
                val triPath = Path().apply {
                    moveTo(tx - 6f, cy - 8f)
                    lineTo(tx + 6f, cy)
                    lineTo(tx - 6f, cy + 8f)
                    close()
                }
                drawPath(path = triPath, color = patternColor.copy(alpha = 0.85f))
            }
        }

        normPattern.contains("h-lines") || normPattern.contains("horizontal") -> {
            val lineCount = 3
            val stepY = (halfW * 1.2f) / (lineCount + 1)
            for (i in 1..lineCount) {
                val ly = (cy - halfW * 0.6f) + i * stepY
                drawLine(
                    color = patternColor.copy(alpha = 0.85f),
                    start = Offset(cx - halfL * 0.85f, ly),
                    end = Offset(cx + halfL * 0.85f, ly),
                    strokeWidth = 2.5f
                )
            }
        }

        normPattern.contains("v-lines") || normPattern.contains("vertical") -> {
            val lineCount = 8
            val stepX = (halfL * 1.6f) / (lineCount + 1)
            for (i in 1..lineCount) {
                val lx = (cx - halfL * 0.8f) + i * stepX
                drawLine(
                    color = patternColor.copy(alpha = 0.80f),
                    start = Offset(lx, cy - halfW * 0.85f),
                    end = Offset(lx, cy + halfW * 0.85f),
                    strokeWidth = 2.2f
                )
            }
        }

        normPattern.contains("chevron") -> {
            val count = 6
            val stepX = (halfL * 1.5f) / (count + 1)
            for (i in 1..count) {
                val chX = (cx - halfL * 0.7f) + i * stepX
                val chevPath = Path().apply {
                    moveTo(chX - 10f, cy - halfW * 0.7f)
                    lineTo(chX + 6f, cy)
                    lineTo(chX - 10f, cy + halfW * 0.7f)
                }
                drawPath(
                    path = chevPath,
                    color = patternColor.copy(alpha = 0.85f),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
            }
        }

        normPattern.contains("diamonds") -> {
            val count = 6
            val stepX = (halfL * 1.5f) / (count + 1)
            for (i in 1..count) {
                val dx = (cx - halfL * 0.75f) + i * stepX
                val diaPath = Path().apply {
                    moveTo(dx, cy - 7f)
                    lineTo(dx + 6f, cy)
                    lineTo(dx, cy + 7f)
                    lineTo(dx - 6f, cy)
                    close()
                }
                drawPath(path = diaPath, color = patternColor.copy(alpha = 0.85f))
            }
        }

        normPattern.contains("scales") || normPattern.contains("natural scale") -> {
            val scaleCount = 8
            val stepX = (halfL * 1.6f) / (scaleCount + 1)
            for (i in 1..scaleCount) {
                val scX = (cx - halfL * 0.8f) + i * stepX
                drawLine(
                    color = patternColor.copy(alpha = 0.55f),
                    start = Offset(scX - 8f, cy - halfW * 0.75f),
                    end = Offset(scX + 8f, cy + halfW * 0.75f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = patternColor.copy(alpha = 0.55f),
                    start = Offset(scX + 8f, cy - halfW * 0.75f),
                    end = Offset(scX - 8f, cy + halfW * 0.75f),
                    strokeWidth = 2f
                )
            }
        }

        normPattern.contains("zebra") -> {
            val barCount = 6
            val stepX = (halfL * 1.5f) / (barCount + 1)
            for (i in 1..barCount) {
                val bx = (cx - halfL * 0.75f) + i * stepX
                val barPath = Path().apply {
                    moveTo(bx - 6f, cy - halfW * 0.85f)
                    lineTo(bx + 6f, cy - halfW * 0.85f)
                    lineTo(bx + 14f, cy + halfW * 0.85f)
                    lineTo(bx + 2f, cy + halfW * 0.85f)
                    close()
                }
                drawPath(path = barPath, color = patternColor.copy(alpha = 0.90f))
            }
        }
    }
}

/**
 * Surface finish shader: Metallic, Gloss, Matte, Holographic, Glitter, Glow, UV Reactive, Natural Scale
 */
private fun DrawScope.drawFinishShader(
    finish: String,
    cx: Float,
    cy: Float,
    halfL: Float,
    halfW: Float
) {
    val normFinish = finish.lowercase().trim()
    when {
        normFinish.contains("metallic") -> {
            val highlightPath = Path().apply {
                moveTo(cx - halfL * 0.8f, cy - halfW * 0.35f)
                cubicTo(
                    cx - halfL * 0.2f, cy - halfW * 0.45f,
                    cx + halfL * 0.3f, cy - halfW * 0.30f,
                    cx + halfL * 0.8f, cy - halfW * 0.15f
                )
            }
            drawPath(
                path = highlightPath,
                color = Color.White.copy(alpha = 0.65f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }

        normFinish.contains("gloss") -> {
            val glossPath = Path().apply {
                moveTo(cx - halfL * 0.75f, cy - halfW * 0.55f)
                cubicTo(
                    cx - halfL * 0.1f, cy - halfW * 0.70f,
                    cx + halfL * 0.4f, cy - halfW * 0.55f,
                    cx + halfL * 0.75f, cy - halfW * 0.25f
                )
            }
            drawPath(
                path = glossPath,
                color = Color.White.copy(alpha = 0.85f),
                style = Stroke(width = 4.5f, cap = StrokeCap.Round)
            )
        }

        normFinish.contains("holographic") -> {
            val holoBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0x0000FFFF),
                    Color(0x5506B6D4),
                    Color(0x66F43F5E),
                    Color(0x66F59E0B),
                    Color(0x00000000)
                ),
                startX = cx - halfL * 0.7f,
                endX = cx + halfL * 0.7f
            )
            drawRect(
                brush = holoBrush,
                topLeft = Offset(cx - halfL, cy - halfW),
                size = Size(halfL * 2f, halfW * 2f)
            )
        }

        normFinish.contains("glitter") -> {
            for (i in 0..18) {
                val gx = cx - halfL * 0.75f + (i * 31f) % (halfL * 1.5f)
                val gy = cy - halfW * 0.45f + (i * 23f) % (halfW * 0.9f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 1.6f,
                    center = Offset(gx, gy)
                )
            }
        }

        normFinish.contains("glow") -> {
            drawCircle(
                color = Color(0x4422C55E),
                radius = halfW * 1.4f,
                center = Offset(cx, cy)
            )
        }

        normFinish.contains("uv reactive") || normFinish.contains("uv") -> {
            val uvBrush = Brush.radialGradient(
                colors = listOf(Color(0x668B5CF6), Color(0x00000000)),
                center = Offset(cx, cy),
                radius = halfL * 0.8f
            )
            drawCircle(brush = uvBrush, radius = halfL * 0.8f, center = Offset(cx, cy))
        }

        normFinish.contains("matte") -> {
            drawRect(
                color = Color(0x220F172A),
                topLeft = Offset(cx - halfL, cy - halfW),
                size = Size(halfL * 2f, halfW * 2f)
            )
        }

        normFinish.contains("natural scale") -> {
            val bioBrush = Brush.verticalGradient(
                colors = listOf(Color(0x3306B6D4), Color(0x110F172A), Color(0x33F59E0B)),
                startY = cy - halfW,
                endY = cy + halfW
            )
            drawRect(
                brush = bioBrush,
                topLeft = Offset(cx - halfL, cy - halfW),
                size = Size(halfL * 2f, halfW * 2f)
            )
        }
    }
}

/**
 * Renders 3D Strike Eye with high-clarity lens reflections and distinct styles.
 */
private fun DrawScope.draw3DEye(
    eyeX: Float,
    eyeY: Float,
    radius: Float,
    eyeStyle: String,
    eyeColorName: String
) {
    val irisColor = resolveEyeColor(eyeColorName)
    val normStyle = eyeStyle.lowercase().trim()

    // Outer Rim
    drawCircle(
        color = Color(0xFFE2E8F0),
        radius = radius,
        center = Offset(eyeX, eyeY)
    )

    // Iris Base
    val irisRadius = radius * 0.82f
    drawCircle(
        color = irisColor,
        radius = irisRadius,
        center = Offset(eyeX, eyeY)
    )

    // Pupil based on Eye Style
    when {
        normStyle.contains("cat-eye") || normStyle.contains("cat") -> {
            // Slit pupil
            val slitPath = Path().apply {
                moveTo(eyeX, eyeY - irisRadius * 0.85f)
                quadraticBezierTo(eyeX + irisRadius * 0.28f, eyeY, eyeX, eyeY + irisRadius * 0.85f)
                quadraticBezierTo(eyeX - irisRadius * 0.28f, eyeY, eyeX, eyeY - irisRadius * 0.85f)
                close()
            }
            drawPath(path = slitPath, color = Color(0xFF0F172A))
        }

        normStyle.contains("oval") -> {
            drawCircle(
                color = Color(0xFF0F172A),
                radius = radius * 0.50f,
                center = Offset(eyeX, eyeY)
            )
        }

        normStyle.contains("holographic") || normStyle.contains("prismatic") -> {
            val prismBrush = Brush.sweepGradient(
                colors = listOf(Color.Cyan, Color.Magenta, Color.Yellow, Color.Cyan),
                center = Offset(eyeX, eyeY)
            )
            drawCircle(
                brush = prismBrush,
                radius = irisRadius * 0.65f,
                center = Offset(eyeX, eyeY)
            )
            drawCircle(
                color = Color(0xFF0F172A),
                radius = radius * 0.40f,
                center = Offset(eyeX, eyeY)
            )
        }

        normStyle.contains("dome") -> {
            drawCircle(
                color = Color(0xFF0F172A),
                radius = radius * 0.52f,
                center = Offset(eyeX, eyeY)
            )
            // Lens curvature highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = radius * 0.65f,
                center = Offset(eyeX, eyeY),
                style = Stroke(width = 1.2f)
            )
        }

        else -> {
            // Default Round / 3D Strike
            drawCircle(
                color = Color(0xFF0F172A),
                radius = radius * 0.45f,
                center = Offset(eyeX, eyeY)
            )
        }
    }

    // Specular Lens Glare Reflection
    drawCircle(
        color = Color.White,
        radius = radius * 0.22f,
        center = Offset(eyeX - radius * 0.28f, eyeY - radius * 0.28f)
    )
}

/**
 * Renders Forged Saltwater Hook & Braided PE Assist Cord.
 */
private fun DrawScope.drawAssistHookAndCord(
    anchorX: Float,
    anchorY: Float,
    hookType: String,
    hookSize: String,
    cordColor: Color,
    showCord: Boolean = true
) {
    val sizeScale = when (hookSize.trim()) {
        "#4" -> 0.75f
        "#2" -> 0.82f
        "#1" -> 0.90f
        "1/0" -> 0.95f
        "2/0" -> 1.05f
        "3/0" -> 1.15f
        "4/0" -> 1.28f
        "5/0" -> 1.40f
        else -> 1.15f
    }

    val cordLength = 40f * sizeScale
    val tieX = anchorX - 8f
    val hookEyeX = if (showCord) tieX - cordLength * 0.4f else tieX
    val hookEyeY = if (showCord) anchorY + 12f else anchorY

    // Braided PE Assist Cord
    if (showCord) {
        drawLine(
            color = cordColor,
            start = Offset(tieX, anchorY),
            end = Offset(hookEyeX, hookEyeY),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )

        // Cord Whipping / Thread Binding Collar
        drawLine(
            color = Color(0xFFF59E0B),
            start = Offset(hookEyeX + 3f, hookEyeY - 2f),
            end = Offset(hookEyeX - 3f, hookEyeY + 2f),
            strokeWidth = 4f
        )
    }

    // Hook Shank & Barb
    val normType = hookType.lowercase().trim()
    val hookPath = Path().apply {
        moveTo(hookEyeX, hookEyeY)
        when {
            normType.contains("wide gap") || normType.contains("ewg") -> {
                lineTo(hookEyeX + 24f * sizeScale, hookEyeY + 22f * sizeScale)
                cubicTo(
                    hookEyeX + 42f * sizeScale, hookEyeY + 34f * sizeScale,
                    hookEyeX + 42f * sizeScale, hookEyeY + 52f * sizeScale,
                    hookEyeX + 20f * sizeScale, hookEyeY + 54f * sizeScale
                )
                cubicTo(
                    hookEyeX + 4f * sizeScale, hookEyeY + 54f * sizeScale,
                    hookEyeX - 2f * sizeScale, hookEyeY + 40f * sizeScale,
                    hookEyeX - 4f * sizeScale, hookEyeY + 24f * sizeScale
                )
                lineTo(hookEyeX, hookEyeY + 28f * sizeScale)
            }
            normType.contains("octopus") -> {
                lineTo(hookEyeX + 18f * sizeScale, hookEyeY + 16f * sizeScale)
                cubicTo(
                    hookEyeX + 30f * sizeScale, hookEyeY + 26f * sizeScale,
                    hookEyeX + 30f * sizeScale, hookEyeY + 42f * sizeScale,
                    hookEyeX + 14f * sizeScale, hookEyeY + 44f * sizeScale
                )
                cubicTo(
                    hookEyeX + 2f * sizeScale, hookEyeY + 44f * sizeScale,
                    hookEyeX - 2f * sizeScale, hookEyeY + 34f * sizeScale,
                    hookEyeX - 4f * sizeScale, hookEyeY + 20f * sizeScale
                )
                lineTo(hookEyeX - 1f * sizeScale, hookEyeY + 24f * sizeScale)
            }
            else -> {
                // Standard O'Shaughnessy / Aberdeen
                lineTo(hookEyeX + 22f * sizeScale, hookEyeY + 18f * sizeScale)
                cubicTo(
                    hookEyeX + 34f * sizeScale, hookEyeY + 28f * sizeScale,
                    hookEyeX + 34f * sizeScale, hookEyeY + 44f * sizeScale,
                    hookEyeX + 18f * sizeScale, hookEyeY + 46f * sizeScale
                )
                cubicTo(
                    hookEyeX + 6f * sizeScale, hookEyeY + 46f * sizeScale,
                    hookEyeX, hookEyeY + 36f * sizeScale,
                    hookEyeX - 2f * sizeScale, hookEyeY + 22f * sizeScale
                )
                lineTo(hookEyeX + 2f * sizeScale, hookEyeY + 26f * sizeScale)
            }
        }
    }

    // Hook Steel
    drawPath(
        path = hookPath,
        color = Color(0xFF94A3B8),
        style = Stroke(width = 3.2f * sizeScale, cap = StrokeCap.Round)
    )
    // Hook Specular Highlight
    drawPath(
        path = hookPath,
        color = Color(0xFFF1F5F9),
        style = Stroke(width = 1.0f * sizeScale, cap = StrokeCap.Round)
    )
}

private fun resolveCordColor(colorName: String): Color {
    val norm = colorName.lowercase().trim()
    return when {
        norm.startsWith("#") -> {
            try {
                Color(android.graphics.Color.parseColor(norm))
            } catch (e: Exception) {
                Color(0xFFDC2626)
            }
        }
        norm == "black" || norm.contains("stealth") -> Color(0xFF1E293B)
        norm == "white" -> Color(0xFFF8FAFC)
        norm == "red" -> Color(0xFFDC2626)
        norm == "orange" || norm.contains("blaze") -> Color(0xFFEA580C)
        norm == "yellow" || norm.contains("chartreuse") -> Color(0xFF84CC16)
        norm == "green" -> Color(0xFF16A34A)
        norm == "blue" || norm.contains("royal") -> Color(0xFF0284C7)
        norm == "pink" -> Color(0xFFEC4899)
        norm == "purple" -> Color(0xFF9333EA)
        norm == "gold" || norm.contains("kevlar") -> Color(0xFFEAB308)
        norm == "silver" || norm == "grey" -> Color(0xFF94A3B8)
        else -> Color(0xFFDC2626)
    }
}

private fun resolveEyeColor(colorName: String): Color {
    val norm = colorName.lowercase().trim()
    return when {
        norm.startsWith("#") -> {
            try {
                Color(android.graphics.Color.parseColor(norm))
            } catch (e: Exception) {
                Color(0xFFDC2626)
            }
        }
        norm == "black" -> Color(0xFF1E293B)
        norm == "white" -> Color(0xFFF8FAFC)
        norm == "green" || norm.contains("emerald") -> Color(0xFF10B981)
        norm == "gold" || norm.contains("solar") -> Color(0xFFEAB308)
        norm == "silver" || norm.contains("chrome") -> Color(0xFFCBD5E1)
        norm == "yellow" || norm.contains("lime") -> Color(0xFF84CC16)
        norm == "orange" -> Color(0xFFEA580C)
        norm == "blue" || norm.contains("sapphire") -> Color(0xFF0284C7)
        else -> Color(0xFFDC2626) // Default Ruby Red
    }
}
