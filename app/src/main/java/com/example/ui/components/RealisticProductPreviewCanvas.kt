package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConfigType
import com.example.data.model.ProductConfiguration
import kotlin.math.*

/**
 * 7Hooks Interactive Realistic Product Canvas
 * Accurately translates all user-configured parameters into a realistic, high-fidelity visual rendering.
 */
@Composable
fun RealisticProductPreviewCanvas(
    config: ProductConfiguration,
    modifier: Modifier = Modifier,
    heightDp: Int = 190
) {
    val baseColor = Color(config.baseColorHex)
    val accentColor = if (config.hasAccentColor) Color(config.accentColorHex) else baseColor

    val animatedBase by animateColorAsState(targetValue = baseColor, label = "baseColor")
    val animatedAccent by animateColorAsState(targetValue = accentColor, label = "accentColor")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A)) // Industrial studio dark slate background
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val w = size.width
            val h = size.height

            // 1. Studio Lighting Floor Vignette
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF090D16)),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.7f
                )
            )

            // Subtle engineering alignment crosshairs
            drawLine(
                color = Color(0xFF334155).copy(alpha = 0.35f),
                start = Offset(0f, h * 0.5f),
                end = Offset(w, h * 0.5f),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            if (config.configType == ConfigType.JIG) {
                drawRealisticJig(w, h, config, animatedBase, animatedAccent)
            } else if (config.configType == ConfigType.LURE) {
                drawRealisticLure(w, h, config, animatedBase, animatedAccent)
            }
        }

        // Top-left specification badge
        Surface(
            color = Color(0xFF0284C7).copy(alpha = 0.9f),
            shape = RoundedCornerShape(bottomEnd = 8.dp),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Text(
                text = "${config.shape.uppercase()} • ${config.weightGrams.toInt()}G • ${config.finishType.uppercase()}",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        // Bottom-right scale indicator
        Surface(
            color = Color(0xFF1E293B).copy(alpha = 0.85f),
            shape = RoundedCornerShape(topStart = 8.dp),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Text(
                text = "L: ${config.lengthMm.toInt()}mm | W: ${config.widthMm.toInt()}mm | ${config.material}",
                color = Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

private fun DrawScope.drawRealisticJig(
    w: Float,
    h: Float,
    config: ProductConfiguration,
    baseColor: Color,
    accentColor: Color
) {
    val cx = w * 0.45f
    val cy = h * 0.52f

    // Proportional dimensions
    val scaleL = (config.lengthMm / 160f).coerceIn(0.6f, 1.4f)
    val scaleW = (config.widthMm / 24f).coerceIn(0.6f, 1.4f)
    val drawL = (w * 0.48f) * scaleL
    val drawW = (h * 0.28f) * scaleW
    val halfL = drawL / 2f
    val halfW = drawW / 2f

    // 1. Hook (behind head or through-wire)
    drawJigHook(cx, cy, halfL, halfW, config)

    // 2. Skirt / Additional Component (rendered under/around head)
    if (config.additionalComponent != "None") {
        drawAdditionalComponent(cx + halfL * 0.3f, cy, halfL, halfW, config, baseColor, accentColor)
    }

    // 3. Main Head Profile Path
    val headPath = Path()
    when (config.shape.lowercase()) {
        "football" -> {
            headPath.moveTo(cx - halfL, cy)
            headPath.cubicTo(cx - halfL * 0.6f, cy - halfW * 1.2f, cx + halfL * 0.3f, cy - halfW * 1.2f, cx + halfL * 0.7f, cy - halfW * 0.4f)
            headPath.lineTo(cx + halfL, cy)
            headPath.lineTo(cx + halfL * 0.7f, cy + halfW * 0.4f)
            headPath.cubicTo(cx + halfL * 0.3f, cy + halfW * 1.2f, cx - halfL * 0.6f, cy + halfW * 1.2f, cx - halfL, cy)
            headPath.close()
        }
        "round" -> {
            headPath.addOval(androidx.compose.ui.geometry.Rect(cx - halfL * 0.7f, cy - halfW, cx + halfL * 0.7f, cy + halfW))
        }
        "arkie" -> {
            headPath.moveTo(cx - halfL, cy - halfW * 0.1f)
            headPath.lineTo(cx - halfL * 0.3f, cy - halfW * 1.1f)
            headPath.lineTo(cx + halfL * 0.8f, cy - halfW * 0.6f)
            headPath.lineTo(cx + halfL, cy)
            headPath.lineTo(cx + halfL * 0.7f, cy + halfW * 0.9f)
            headPath.lineTo(cx - halfL * 0.4f, cy + halfW * 0.9f)
            headPath.close()
        }
        "swimbait" -> {
            headPath.moveTo(cx - halfL, cy)
            headPath.cubicTo(cx - halfL * 0.8f, cy - halfW * 0.9f, cx + halfL * 0.2f, cy - halfW * 0.9f, cx + halfL, cy - halfW * 0.3f)
            headPath.lineTo(cx + halfL, cy + halfW * 0.3f)
            headPath.cubicTo(cx + halfL * 0.2f, cy + halfW * 0.9f, cx - halfL * 0.8f, cy + halfW * 0.9f, cx - halfL, cy)
            headPath.close()
        }
        "bullet" -> {
            headPath.moveTo(cx - halfL, cy)
            headPath.lineTo(cx + halfL * 0.8f, cy - halfW)
            headPath.lineTo(cx + halfL, cy)
            headPath.lineTo(cx + halfL * 0.8f, cy + halfW)
            headPath.close()
        }
        else -> {
            // Versatile flutter profile
            headPath.moveTo(cx - halfL, cy)
            headPath.cubicTo(cx - halfL * 0.7f, cy - halfW, cx + halfL * 0.2f, cy - halfW * 0.95f, cx + halfL * 0.85f, cy - halfW * 0.4f)
            headPath.lineTo(cx + halfL, cy)
            headPath.cubicTo(cx + halfL * 0.85f, cy + halfW * 0.4f, cx + halfL * 0.2f, cy + halfW * 0.95f, cx - halfL * 0.7f, cy + halfW)
            headPath.close()
        }
    }

    // Glow aura if enabled
    if (config.hasGlow || config.finishType.equals("Glow", ignoreCase = true)) {
        drawPath(
            path = headPath,
            color = Color(0xFF22C55E).copy(alpha = 0.3f),
            style = Stroke(width = 10f)
        )
    }

    // Base body fill with finish shader
    val bodyBrush = createFinishBrush(config.finishType, baseColor, accentColor, cx, cy, halfL, halfW)
    drawPath(path = headPath, brush = bodyBrush)

    // Secondary Accent Color layer (bottom keel or lateral stripe)
    if (config.hasAccentColor) {
        val accentPath = Path().apply {
            moveTo(cx - halfL * 0.8f, cy + halfW * 0.2f)
            cubicTo(cx - halfL * 0.2f, cy + halfW * 0.7f, cx + halfL * 0.4f, cy + halfW * 0.8f, cx + halfL * 0.9f, cy + halfW * 0.3f)
            lineTo(cx + halfL * 0.9f, cy + halfW * 0.6f)
            cubicTo(cx + halfL * 0.3f, cy + halfW * 1.0f, cx - halfL * 0.3f, cy + halfW * 0.9f, cx - halfL * 0.8f, cy + halfW * 0.5f)
            close()
        }
        drawPath(path = accentPath, color = accentColor.copy(alpha = 0.85f))
    }

    // 4. Surface Pattern Overlay
    drawSurfacePattern(cx, cy, halfL, halfW, config.patternName, accentColor)

    // 5. Realistic Finish Highlights (Specular sheen, glitter, scales)
    applyFinishHighlights(headPath, config.finishType, cx, cy, halfL, halfW)

    // 6. 3D Bevel Outline
    drawPath(
        path = headPath,
        color = Color(0xFF0F172A).copy(alpha = 0.6f),
        style = Stroke(width = 1.5f)
    )

    // 7. Eye Configuration
    val eyeX = cx - halfL * 0.55f
    val eyeY = cy - halfW * 0.25f
    drawRealisticEye(eyeX, eyeY, config.eyeStyle, config.eyeShape, Color(config.eyeColorHex))

    // 8. Line-tie / front through-wire eyelet
    drawEyelet(cx - halfL - 4f, cy, 5.5f)

    // 9. Weed Guard (if enabled)
    if (config.hasWeedGuard) {
        drawWeedGuard(cx - halfL * 0.3f, cy - halfW * 0.5f, cx + halfL * 0.8f, cy - halfW * 1.3f)
    }
}

private fun DrawScope.drawRealisticLure(
    w: Float,
    h: Float,
    config: ProductConfiguration,
    baseColor: Color,
    accentColor: Color
) {
    val cx = w * 0.48f
    val cy = h * 0.52f

    val scaleL = (config.lengthMm / 150f).coerceIn(0.65f, 1.35f)
    val scaleW = (config.widthMm / 24f).coerceIn(0.65f, 1.35f)
    val drawL = (w * 0.55f) * scaleL
    val drawW = (h * 0.26f) * scaleW
    val halfL = drawL / 2f
    val halfW = drawW / 2f

    // 1. Treble Hooks (Front belly + Rear tail)
    val hookQty = config.hookQuantity.coerceIn(1, 3)
    if (hookQty >= 2) {
        drawTrebleHook(cx - halfL * 0.1f, cy + halfW * 0.95f, isSaltwater = true)
    }
    drawTrebleHook(cx + halfL * 0.95f, cy, isSaltwater = true)

    // 2. Diving Bib / Lip (Crankbait, Minnow, Jerkbait)
    if (config.divingDepthMeters > 0.5f) {
        val bibLength = (config.divingDepthMeters * 5f).coerceIn(12f, 28f)
        val bibAngleRad = Math.toRadians(35.0)
        val bibPath = Path().apply {
            moveTo(cx - halfL, cy + 2f)
            lineTo(cx - halfL - bibLength * cos(bibAngleRad).toFloat(), cy + bibLength * sin(bibAngleRad).toFloat())
            lineTo(cx - halfL - (bibLength - 4f) * cos(bibAngleRad).toFloat(), cy + (bibLength + 6f) * sin(bibAngleRad).toFloat())
            lineTo(cx - halfL + 6f, cy + 8f)
            close()
        }
        drawPath(path = bibPath, color = Color(0xFFE2E8F0).copy(alpha = 0.65f))
        drawPath(path = bibPath, color = Color(0xFF94A3B8), style = Stroke(width = 1f))
    }

    // 3. Lure Body Profile
    val lureBody = Path().apply {
        moveTo(cx - halfL, cy)
        cubicTo(cx - halfL * 0.7f, cy - halfW * 1.05f, cx - halfL * 0.1f, cy - halfW * 1.15f, cx + halfL * 0.6f, cy - halfW * 0.55f)
        lineTo(cx + halfL, cy)
        cubicTo(cx + halfL * 0.6f, cy + halfW * 0.55f, cx - halfL * 0.1f, cy + halfW * 1.0f, cx - halfL * 0.7f, cy + halfW * 0.75f)
        close()
    }

    // Glow aura
    if (config.hasGlow || config.finishType.equals("Glow", ignoreCase = true)) {
        drawPath(path = lureBody, color = Color(0xFF06B6D4).copy(alpha = 0.25f), style = Stroke(width = 8f))
    }

    val bodyBrush = createFinishBrush(config.finishType, baseColor, accentColor, cx, cy, halfL, halfW)
    drawPath(path = lureBody, brush = bodyBrush)

    // Dorsal / Belly Accent Color
    if (config.hasAccentColor) {
        val dorsalPath = Path().apply {
            moveTo(cx - halfL * 0.9f, cy - halfW * 0.4f)
            cubicTo(cx - halfL * 0.2f, cy - halfW * 1.1f, cx + halfL * 0.3f, cy - halfW * 1.0f, cx + halfL * 0.9f, cy - halfW * 0.2f)
            lineTo(cx + halfL * 0.9f, cy - halfW * 0.5f)
            cubicTo(cx + halfL * 0.3f, cy - halfW * 1.25f, cx - halfL * 0.2f, cy - halfW * 1.25f, cx - halfL * 0.9f, cy - halfW * 0.5f)
            close()
        }
        drawPath(path = dorsalPath, color = accentColor.copy(alpha = 0.9f))
    }

    // Surface Pattern
    drawSurfacePattern(cx, cy, halfL, halfW, config.patternName, accentColor)

    // Finish Highlights
    applyFinishHighlights(lureBody, config.finishType, cx, cy, halfL, halfW)

    // Lateral Line
    val lateralLine = Path().apply {
        moveTo(cx - halfL * 0.65f, cy)
        cubicTo(cx - halfL * 0.1f, cy + 2f, cx + halfL * 0.4f, cy - 1f, cx + halfL * 0.85f, cy)
    }
    drawPath(
        path = lateralLine,
        color = Color.White.copy(alpha = 0.45f),
        style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
    )

    // Eye
    val eyeX = cx - halfL * 0.75f
    val eyeY = cy - halfW * 0.35f
    drawRealisticEye(eyeX, eyeY, config.eyeStyle, config.eyeShape, Color(config.eyeColorHex))

    // Front Eyelet
    drawEyelet(cx - halfL - 3f, cy, 5f)
}

private fun DrawScope.createFinishBrush(
    finishType: String,
    baseColor: Color,
    accentColor: Color,
    cx: Float,
    cy: Float,
    halfL: Float,
    halfW: Float
): Brush {
    return when (finishType.lowercase()) {
        "metallic" -> {
            Brush.linearGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.9f),
                    Color(0xFFE2E8F0),
                    baseColor,
                    baseColor.copy(alpha = 0.7f)
                ),
                start = Offset(cx - halfL, cy - halfW),
                end = Offset(cx + halfL, cy + halfW)
            )
        }
        "holographic" -> {
            Brush.linearGradient(
                colors = listOf(
                    baseColor,
                    Color(0xFF38BDF8),
                    Color(0xFFA855F7),
                    Color(0xFFF43F5E),
                    Color(0xFFFACC15),
                    baseColor
                ),
                start = Offset(cx - halfL, cy - halfW * 1.2f),
                end = Offset(cx + halfL, cy + halfW * 1.2f)
            )
        }
        "gloss" -> {
            Brush.verticalGradient(
                colors = listOf(
                    baseColor.copy(alpha = 1f),
                    Color.White.copy(alpha = 0.35f),
                    baseColor,
                    baseColor.copy(alpha = 0.8f)
                ),
                startY = cy - halfW,
                endY = cy + halfW
            )
        }
        "matte" -> {
            Brush.radialGradient(
                colors = listOf(baseColor, baseColor.copy(alpha = 0.9f)),
                center = Offset(cx, cy),
                radius = halfL
            )
        }
        else -> {
            Brush.verticalGradient(
                colors = listOf(baseColor, accentColor.copy(alpha = 0.8f)),
                startY = cy - halfW,
                endY = cy + halfW
            )
        }
    }
}

private fun DrawScope.applyFinishHighlights(
    bodyPath: Path,
    finishType: String,
    cx: Float,
    cy: Float,
    halfL: Float,
    halfW: Float
) {
    when (finishType.lowercase()) {
        "glitter" -> {
            // Scattered light specs
            val random = kotlin.random.Random(42)
            for (i in 0 until 35) {
                val rx = cx - halfL * 0.7f + random.nextFloat() * (halfL * 1.4f)
                val ry = cy - halfW * 0.7f + random.nextFloat() * (halfW * 1.4f)
                drawCircle(Color.White.copy(alpha = 0.75f), radius = 1.2f, center = Offset(rx, ry))
            }
        }
        "gloss" -> {
            // High-refraction sharp highlight streak
            val streak = Path().apply {
                moveTo(cx - halfL * 0.6f, cy - halfW * 0.55f)
                cubicTo(cx - halfL * 0.2f, cy - halfW * 0.7f, cx + halfL * 0.3f, cy - halfW * 0.6f, cx + halfL * 0.7f, cy - halfW * 0.3f)
            }
            drawPath(path = streak, color = Color.White.copy(alpha = 0.65f), style = Stroke(width = 2.5f))
        }
        "natural scale" -> {
            // Cross-mesh scale simulation
            val gridStep = 7f
            var x = cx - halfL * 0.6f
            while (x < cx + halfL * 0.7f) {
                drawLine(
                    color = Color.White.copy(alpha = 0.18f),
                    start = Offset(x, cy - halfW * 0.6f),
                    end = Offset(x + 10f, cy + halfW * 0.6f),
                    strokeWidth = 0.8f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.18f),
                    start = Offset(x + 10f, cy - halfW * 0.6f),
                    end = Offset(x, cy + halfW * 0.6f),
                    strokeWidth = 0.8f
                )
                x += gridStep
            }
        }
    }
}

private fun DrawScope.drawSurfacePattern(
    cx: Float,
    cy: Float,
    halfL: Float,
    halfW: Float,
    patternName: String,
    accentColor: Color
) {
    val pColor = accentColor.copy(alpha = 0.85f)
    when (patternName.lowercase()) {
        "dots" -> {
            val dotSpacing = 16f
            var dx = cx - halfL * 0.5f
            while (dx < cx + halfL * 0.6f) {
                drawCircle(color = pColor, radius = 2.8f, center = Offset(dx, cy - halfW * 0.2f))
                drawCircle(color = pColor, radius = 2.0f, center = Offset(dx + 8f, cy + halfW * 0.2f))
                dx += dotSpacing
            }
        }
        "stripes" -> {
            val stripeSpacing = 18f
            var sx = cx - halfL * 0.5f
            while (sx < cx + halfL * 0.7f) {
                drawLine(
                    color = pColor,
                    start = Offset(sx, cy - halfW * 0.7f),
                    end = Offset(sx - 6f, cy + halfW * 0.7f),
                    strokeWidth = 3.5f
                )
                sx += stripeSpacing
            }
        }
        "chevron" -> {
            val spacing = 22f
            var cxStep = cx - halfL * 0.4f
            while (cxStep < cx + halfL * 0.6f) {
                val chev = Path().apply {
                    moveTo(cxStep - 6f, cy - halfW * 0.6f)
                    lineTo(cxStep + 4f, cy)
                    lineTo(cxStep - 6f, cy + halfW * 0.6f)
                }
                drawPath(path = chev, color = pColor, style = Stroke(width = 2.5f))
                cxStep += spacing
            }
        }
        "tiger" -> {
            val spacing = 20f
            var tx = cx - halfL * 0.4f
            while (tx < cx + halfL * 0.6f) {
                val tPath = Path().apply {
                    moveTo(tx, cy - halfW * 0.7f)
                    lineTo(tx + 5f, cy - halfW * 0.1f)
                    lineTo(tx - 3f, cy + halfW * 0.7f)
                }
                drawPath(path = tPath, color = pColor, style = Stroke(width = 3.2f))
                tx += spacing
            }
        }
    }
}

private fun DrawScope.drawRealisticEye(
    x: Float,
    y: Float,
    style: String,
    shape: String,
    irisColor: Color
) {
    val r = 7f
    // Outer socket ring
    drawCircle(Color(0xFF0F172A), radius = r + 1.5f, center = Offset(x, y))

    // Iris base
    val irisBrush = if (style.contains("Holographic", ignoreCase = true) || style.contains("3D", ignoreCase = true)) {
        Brush.radialGradient(
            colors = listOf(irisColor, irisColor.copy(alpha = 0.6f), Color(0xFF0F172A)),
            center = Offset(x, y),
            radius = r
        )
    } else {
        Brush.radialGradient(colors = listOf(irisColor, irisColor), center = Offset(x, y), radius = r)
    }
    drawCircle(brush = irisBrush, radius = r, center = Offset(x, y))

    // Pupil
    when (shape.lowercase()) {
        "cat-eye" -> {
            drawOval(
                color = Color.Black,
                topLeft = Offset(x - 1.5f, y - 4f),
                size = Size(3f, 8f)
            )
        }
        "oval" -> {
            drawOval(
                color = Color.Black,
                topLeft = Offset(x - 3.5f, y - 4.5f),
                size = Size(7f, 9f)
            )
        }
        else -> {
            drawCircle(Color.Black, radius = 3.2f, center = Offset(x, y))
        }
    }

    // 3D Dome reflection highlight
    drawCircle(Color.White, radius = 1.2f, center = Offset(x - 2f, y - 2f))
}

private fun DrawScope.drawEyelet(x: Float, y: Float, radius: Float) {
    drawCircle(Color(0xFF94A3B8), radius = radius, center = Offset(x, y), style = Stroke(width = 2.2f))
}

private fun DrawScope.drawJigHook(cx: Float, cy: Float, halfL: Float, halfW: Float, config: ProductConfiguration) {
    val hookShankX = cx + halfL * 0.2f
    val hookBendX = cx + halfL * 1.35f
    val hookPointX = cx + halfL * 1.15f
    val hookPointY = cy - halfW * 1.2f

    val hookPath = Path().apply {
        moveTo(hookShankX, cy)
        lineTo(hookBendX, cy)
        cubicTo(hookBendX + 16f, cy, hookBendX + 16f, hookPointY + 8f, hookPointX, hookPointY)
    }
    // Forged steel appearance
    drawPath(path = hookPath, color = Color(0xFF64748B), style = Stroke(width = 3.2f, cap = StrokeCap.Round))
    drawPath(path = hookPath, color = Color(0xFFE2E8F0), style = Stroke(width = 1.2f, cap = StrokeCap.Round))

    // Barb
    drawLine(
        color = Color(0xFF64748B),
        start = Offset(hookPointX, hookPointY),
        end = Offset(hookPointX + 4f, hookPointY + 5f),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawTrebleHook(x: Float, y: Float, isSaltwater: Boolean) {
    val shankLen = 22f
    // Shank
    drawLine(Color(0xFF64748B), Offset(x, y), Offset(x, y + shankLen), strokeWidth = 2.2f)

    // Left barb curve
    val leftCurve = Path().apply {
        moveTo(x, y + shankLen)
        cubicTo(x - 12f, y + shankLen + 4f, x - 12f, y + shankLen - 8f, x - 8f, y + shankLen - 12f)
    }
    drawPath(path = leftCurve, color = Color(0xFF64748B), style = Stroke(width = 2f))

    // Right barb curve
    val rightCurve = Path().apply {
        moveTo(x, y + shankLen)
        cubicTo(x + 12f, y + shankLen + 4f, x + 12f, y + shankLen - 8f, x + 8f, y + shankLen - 12f)
    }
    drawPath(path = rightCurve, color = Color(0xFF64748B), style = Stroke(width = 2f))
}

private fun DrawScope.drawAdditionalComponent(
    startX: Float,
    startY: Float,
    halfL: Float,
    halfW: Float,
    config: ProductConfiguration,
    baseColor: Color,
    accentColor: Color
) {
    when (config.additionalComponent.lowercase()) {
        "silicone" -> {
            // Multi-strand skirt
            val random = kotlin.random.Random(101)
            for (i in 0 until 24) {
                val endX = startX + halfL * 0.8f + random.nextFloat() * 20f
                val endY = startY + (random.nextFloat() - 0.5f) * halfW * 2.8f
                val strandColor = if (i % 2 == 0) baseColor else accentColor
                drawLine(
                    color = strandColor.copy(alpha = 0.8f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.8f,
                    cap = StrokeCap.Round
                )
            }
        }
        "feather" -> {
            val featherPath = Path().apply {
                moveTo(startX, startY)
                cubicTo(startX + 25f, startY - 12f, startX + 45f, startY - 8f, startX + 55f, startY)
                cubicTo(startX + 45f, startY + 8f, startX + 25f, startY + 12f, startX, startY)
            }
            drawPath(featherPath, color = Color.White.copy(alpha = 0.65f))
        }
        "hair" -> {
            for (i in 0 until 18) {
                val endX = startX + halfL * 0.7f
                val endY = startY + (i - 9) * 2.2f
                drawLine(Color(0xFFF1F5F9).copy(alpha = 0.7f), Offset(startX, startY), Offset(endX, endY), strokeWidth = 1.2f)
            }
        }
        "rubber", "soft plastic" -> {
            val tailPath = Path().apply {
                moveTo(startX, startY - 6f)
                cubicTo(startX + 30f, startY - 14f, startX + 50f, startY + 12f, startX + 65f, startY)
                lineTo(startX + 65f, startY + 6f)
                cubicTo(startX + 50f, startY + 18f, startX + 30f, startY - 8f, startX, startY + 6f)
                close()
            }
            drawPath(tailPath, color = accentColor.copy(alpha = 0.85f))
        }
    }
}

private fun DrawScope.drawWeedGuard(startX: Float, startY: Float, endX: Float, endY: Float) {
    for (i in -2..2) {
        drawLine(
            color = Color(0xFF1E293B).copy(alpha = 0.85f),
            start = Offset(startX, startY),
            end = Offset(endX + i * 2f, endY + i * 3f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
    }
}
