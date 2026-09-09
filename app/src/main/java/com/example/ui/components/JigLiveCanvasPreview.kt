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
                            selectedContainerColor = Color(0xFF0284C7),
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
 * Geometry -> Color Zones -> Patterns -> Finishes -> 3D Eyes -> Assist Hook -> Cord -> Rings
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

    // 2. BASE COLOR LAYER (Step 4+)
    val mainColor = if (activeStep >= 4) Color(config.mainColorHex) else Color(0xFF94A3B8)
    val secondaryColor = if (activeStep >= 4) Color(config.secondaryColorHex) else Color(0xFF64748B)

    // Two-tone gradient across body (Dorsal to Keel)
    val bodyBrush = Brush.verticalGradient(
        colors = listOf(
            mainColor,
            mainColor.copy(alpha = 0.92f),
            secondaryColor
        ),
        startY = cy - halfW,
        endY = cy + halfW
    )

    drawPath(path = bodyPath, brush = bodyBrush)

    // 3. PATTERN LAYER (Step 5+)
    if (activeStep >= 5 && config.pattern != "Solid") {
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

    // 4. MATERIAL & SURFACE FINISH LAYER (Step 6+)
    if (activeStep >= 6) {
        drawFinishShader(
            finish = config.finish,
            cx = cx,
            cy = cy,
            halfL = halfL,
            halfW = halfW
        )
    }

    // Body Contour Border / Edge Definition
    drawPath(
        path = bodyPath,
        color = Color(0x660F172A),
        style = Stroke(width = 1.5f)
    )

    // 5. 3D EYE LAYER (Step 7+)
    if (activeStep >= 7) {
        val eyeX = cx - halfL + (bodyLength * template.eyeAnchorX)
        val eyeY = cy - halfW * 0.25f
        val eyeRadius = (bodyWidth * 0.22f).coerceIn(5f, 14f)

        draw3DEye(
            eyeX = eyeX,
            eyeY = eyeY,
            radius = eyeRadius,
            eyeStyle = config.eyeStyle,
            eyeColorName = config.eyeColor
        )
    }

    // 6. FRONT RING (Step 10+)
    if (activeStep >= 10 && config.frontRing != "None") {
        val noseX = cx - halfL
        val ringRadius = if (config.frontRing.contains("Heavy", ignoreCase = true)) 10f else 7.5f
        val strokeW = if (config.frontRing.contains("Heavy", ignoreCase = true)) 3.5f else 2.2f

        // Front Solid Ring
        drawCircle(
            color = Color(0xFFCBD5E1),
            radius = ringRadius,
            center = Offset(noseX - ringRadius * 0.7f, cy),
            style = Stroke(width = strokeW)
        )
        // Inner highlight
        drawCircle(
            color = Color(0xFF64748B),
            radius = ringRadius - strokeW / 2f,
            center = Offset(noseX - ringRadius * 0.7f, cy),
            style = Stroke(width = 0.8f)
        )
    }

    // 7. BACK RING (Step 10+)
    if (activeStep >= 10 && config.backRing != "None") {
        val tailX = cx + halfL
        val ringRadius = if (config.backRing.contains("Heavy", ignoreCase = true)) 9f else 7f
        val strokeW = if (config.backRing.contains("Heavy", ignoreCase = true)) 3.2f else 2f

        drawCircle(
            color = Color(0xFFCBD5E1),
            radius = ringRadius,
            center = Offset(tailX + ringRadius * 0.7f, cy),
            style = Stroke(width = strokeW)
        )
    }

    // 8. ASSIST HOOK & BRAIDED CORD LAYER (Step 8 & 9)
    if (activeStep >= 8 && config.assistHook != "None") {
        val anchorX = cx - halfL
        val cordColor = if (activeStep >= 9) resolveCordColor(config.assistCordColor) else Color(0xFFDC2626)

        drawAssistHookAndCord(
            anchorX = anchorX,
            anchorY = cy,
            hookType = config.assistHook,
            cordColor = cordColor
        )
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
 * Renders tactical patterns: Tiger stripes, Strike dots, Chevrons, Scales, Zebra
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
    when (pattern.lowercase()) {
        "tiger" -> {
            // Transverse tapered stripes across body
            val stripeCount = 6
            val stepX = (halfL * 1.6f) / (stripeCount + 1)
            for (i in 1..stripeCount) {
                val sx = (cx - halfL * 0.8f) + i * stepX
                val stripePath = Path().apply {
                    moveTo(sx - 3f, cy - halfW * 0.85f)
                    lineTo(sx + 3f, cy - halfW * 0.85f)
                    lineTo(sx + 7f, cy + halfW * 0.85f)
                    lineTo(sx + 2f, cy + halfW * 0.85f)
                    close()
                }
                drawPath(path = stripePath, color = patternColor.copy(alpha = 0.88f))
            }
        }

        "dots" -> {
            // Precision laser strike dots along the lateral line
            val dotCount = 8
            val stepX = (halfL * 1.5f) / (dotCount + 1)
            for (i in 1..dotCount) {
                val dx = (cx - halfL * 0.75f) + i * stepX
                val dy = cy + (if (i % 2 == 0) -halfW * 0.20f else halfW * 0.20f)
                drawCircle(
                    color = patternColor,
                    radius = 3.8f,
                    center = Offset(dx, dy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = 1.2f,
                    center = Offset(dx - 1f, dy - 1f)
                )
            }
        }

        "chevron" -> {
            // Forward hydrodynamic arrows
            val count = 5
            val stepX = (halfL * 1.5f) / (count + 1)
            for (i in 1..count) {
                val chX = (cx - halfL * 0.7f) + i * stepX
                val chevPath = Path().apply {
                    moveTo(chX - 10f, cy - halfW * 0.7f)
                    lineTo(chX + 5f, cy)
                    lineTo(chX - 10f, cy + halfW * 0.7f)
                }
                drawPath(
                    path = chevPath,
                    color = patternColor.copy(alpha = 0.85f),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
        }

        "scales" -> {
            // Cross-hatched diamond micro-scales
            val scaleCount = 7
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

        "zebra" -> {
            // Angled high-contrast glow bars
            val barCount = 5
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
 * Surface finish shader: Metallic, Gloss, Matte, Holographic, Glitter, Glow, UV Reactive
 */
private fun DrawScope.drawFinishShader(
    finish: String,
    cx: Float,
    cy: Float,
    halfL: Float,
    halfW: Float
) {
    when (finish.lowercase()) {
        "metallic" -> {
            // Specular directional ridge reflection
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

        "gloss" -> {
            // Controlled sharp white specular curve along dorsal edge
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

        "holographic" -> {
            // Multi-spectral iridescence sheen
            val holoBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0x0000FFFF),
                    Color(0x5506B6D4), // Cyan
                    Color(0x66F43F5E), // Magenta
                    Color(0x66F59E0B), // Gold
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

        "glitter" -> {
            // Micro sparkle flecks
            for (i in 0..16) {
                val gx = cx - halfL * 0.7f + (i * 27f) % (halfL * 1.4f)
                val gy = cy - halfW * 0.4f + (i * 19f) % (halfW * 0.8f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 1.5f,
                    center = Offset(gx, gy)
                )
            }
        }

        "glow" -> {
            // Luminous phosphor aura
            drawCircle(
                color = Color(0x4422C55E),
                radius = halfW * 1.4f,
                center = Offset(cx, cy)
            )
        }

        "uv reactive" -> {
            // High energy neon-violet wash
            val uvBrush = Brush.radialGradient(
                colors = listOf(Color(0x668B5CF6), Color(0x00000000)),
                center = Offset(cx, cy),
                radius = halfL * 0.8f
            )
            drawCircle(brush = uvBrush, radius = halfL * 0.8f, center = Offset(cx, cy))
        }

        "matte" -> {
            // Diffuse overlay reducing specular glare
            drawRect(
                color = Color(0x220F172A),
                topLeft = Offset(cx - halfL, cy - halfW),
                size = Size(halfL * 2f, halfW * 2f)
            )
        }
    }
}

/**
 * Renders 3D Strike Eye with high-clarity lens reflections.
 */
private fun DrawScope.draw3DEye(
    eyeX: Float,
    eyeY: Float,
    radius: Float,
    eyeStyle: String,
    eyeColorName: String
) {
    val irisColor = resolveEyeColor(eyeColorName)

    // Outer Chrome Rim
    drawCircle(
        color = Color(0xFFE2E8F0),
        radius = radius,
        center = Offset(eyeX, eyeY)
    )

    // Iris Base
    drawCircle(
        color = irisColor,
        radius = radius * 0.82f,
        center = Offset(eyeX, eyeY)
    )

    // Deep Black Pupil
    drawCircle(
        color = Color(0xFF0F172A),
        radius = radius * 0.45f,
        center = Offset(eyeX, eyeY)
    )

    // Specular Lens Glare Reflection
    drawCircle(
        color = Color.White,
        radius = radius * 0.22f,
        center = Offset(eyeX - radius * 0.28f, eyeY - radius * 0.28f)
    )
}

/**
 * Renders Forged Saltwater Assist Hook & Braided PE Cord.
 */
private fun DrawScope.drawAssistHookAndCord(
    anchorX: Float,
    anchorY: Float,
    hookType: String,
    cordColor: Color
) {
    val cordLength = 42f
    val tieX = anchorX - 8f
    val hookEyeX = tieX - cordLength * 0.4f
    val hookEyeY = anchorY + 12f

    // Braided PE Cord (bound from tie ring to hook shank)
    drawLine(
        color = cordColor,
        start = Offset(tieX, anchorY),
        end = Offset(hookEyeX, hookEyeY),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )

    // Cord Whipping / Thread Binding Collar
    drawLine(
        color = Color(0xFFF59E0B), // Golden whipping thread
        start = Offset(hookEyeX + 3f, hookEyeY - 2f),
        end = Offset(hookEyeX - 3f, hookEyeY + 2f),
        strokeWidth = 4f
    )

    // Forged Stainless Steel Hook Shank & Barb
    val hookPath = Path().apply {
        moveTo(hookEyeX, hookEyeY)
        lineTo(hookEyeX + 22f, hookEyeY + 18f)
        cubicTo(
            hookEyeX + 34f, hookEyeY + 28f,
            hookEyeX + 34f, hookEyeY + 44f,
            hookEyeX + 18f, hookEyeY + 46f
        )
        cubicTo(
            hookEyeX + 6f, hookEyeY + 46f,
            hookEyeX, hookEyeY + 36f,
            hookEyeX - 2f, hookEyeY + 22f
        )
        // Barb
        lineTo(hookEyeX + 2f, hookEyeY + 26f)
    }

    // Hook Steel
    drawPath(
        path = hookPath,
        color = Color(0xFF94A3B8),
        style = Stroke(width = 3.2f, cap = StrokeCap.Round)
    )
    // Hook Highlight
    drawPath(
        path = hookPath,
        color = Color(0xFFF1F5F9),
        style = Stroke(width = 1.0f, cap = StrokeCap.Round)
    )
}

private fun resolveCordColor(colorName: String): Color {
    return when (colorName.lowercase()) {
        "blue", "royal blue" -> Color(0xFF0284C7)
        "black", "stealth black" -> Color(0xFF1E293B)
        "orange", "blaze orange" -> Color(0xFFEA580C)
        "chartreuse", "chartreuse glow" -> Color(0xFF84CC16)
        "gold", "kevlar gold" -> Color(0xFFEAB308)
        else -> Color(0xFFDC2626) // Default Red
    }
}

private fun resolveEyeColor(colorName: String): Color {
    return when (colorName.lowercase()) {
        "emerald green", "green" -> Color(0xFF10B981)
        "gold", "solar gold" -> Color(0xFFEAB308)
        "silver", "chrome" -> Color(0xFFCBD5E1)
        "luminous lime", "lime" -> Color(0xFF84CC16)
        "sapphire", "blue" -> Color(0xFF0284C7)
        else -> Color(0xFFDC2626) // Default Ruby Red
    }
}
