package com.example.ui.components

import android.graphics.DashPathEffect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JigPatternType
import com.example.data.model.ProductConfiguration

@OptIn(ExperimentalTextApi::class)
@Composable
fun JigEngineeringCanvas(
    config: ProductConfiguration,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color(0xFFFCFDFD), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Subtle Engineering Grid
            val gridSpacing = 20.dp.toPx()
            val gridColor = Color(0x180284C7)
            var x = 0f
            while (x <= canvasWidth) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, canvasHeight), strokeWidth = 1f)
                x += gridSpacing
            }
            var y = 0f
            while (y <= canvasHeight) {
                drawLine(gridColor, Offset(0f, y), Offset(canvasWidth, y), strokeWidth = 1f)
                y += gridSpacing
            }

            val centerX = canvasWidth * 0.46f
            val centerY = canvasHeight * 0.52f

            // Dynamic scale according to configuration parameters
            val scaleLength = (config.lengthMm / 200f).coerceIn(0.55f, 1.35f)
            val scaleWidth = (config.widthMm / 30f).coerceIn(0.55f, 1.35f)
            val drawLength = 170.dp.toPx() * scaleLength
            val drawWidth = 36.dp.toPx() * scaleWidth

            val halfL = drawLength / 2f
            val halfW = drawWidth / 2f

            // 2. Centerlines (Red dashed technical centerline)
            val centerLineColor = Color(0xFFEF4444)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 4f, 2f, 4f), 0f)
            drawLine(
                color = centerLineColor,
                start = Offset(centerX - halfL - 30.dp.toPx(), centerY),
                end = Offset(centerX + halfL + 30.dp.toPx(), centerY),
                strokeWidth = 1.2f,
                pathEffect = dashEffect
            )
            drawLine(
                color = centerLineColor,
                start = Offset(centerX, centerY - halfW - 25.dp.toPx()),
                end = Offset(centerX, centerY + halfW + 25.dp.toPx()),
                strokeWidth = 1.2f,
                pathEffect = dashEffect
            )

            // 3. Main Jig Body Path
            val bodyPath = Path().apply {
                moveTo(centerX - halfL, centerY)
                cubicTo(
                    centerX - halfL * 0.6f, centerY - halfW * 0.85f,
                    centerX + halfL * 0.1f, centerY - halfW,
                    centerX + halfL * 0.6f, centerY - halfW * 0.7f
                )
                lineTo(centerX + halfL, centerY)
                cubicTo(
                    centerX + halfL * 0.6f, centerY + halfW * 0.7f,
                    centerX + halfL * 0.1f, centerY + halfW,
                    centerX - halfL * 0.6f, centerY + halfW * 0.85f
                )
                close()
            }

            // Fill body with gradient
            drawPath(
                path = bodyPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(config.baseColorHex).copy(alpha = 0.85f),
                        Color(config.accentColorHex).copy(alpha = 0.85f)
                    ),
                    startY = centerY - halfW,
                    endY = centerY + halfW
                )
            )

            // Crisp CAD outline
            drawPath(
                path = bodyPath,
                color = Color(0xFF0F172A),
                style = Stroke(width = 2.dp.toPx())
            )

            // Center Keel Line
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(centerX - halfL + 8.dp.toPx(), centerY),
                end = Offset(centerX + halfL - 8.dp.toPx(), centerY),
                strokeWidth = 1.8f
            )

            // Pattern Rendering
            when (config.patternType) {
                JigPatternType.DOT_PATTERN -> {
                    val dotColor = Color(config.accentColorHex)
                    for (i in -3..3) {
                        val dx = centerX + i * 14.dp.toPx() * scaleLength
                        drawCircle(dotColor, radius = 2.8.dp.toPx(), center = Offset(dx, centerY - 5.dp.toPx()))
                        drawCircle(dotColor, radius = 2.8.dp.toPx(), center = Offset(dx + 7.dp.toPx(), centerY + 5.dp.toPx()))
                    }
                }
                else -> {
                    for (i in -3..3) {
                        val sx = centerX + i * 14.dp.toPx() * scaleLength
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(sx - 6.dp.toPx(), centerY - 10.dp.toPx()),
                            end = Offset(sx + 6.dp.toPx(), centerY + 10.dp.toPx()),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }

            // Eyelet Rings
            val eyeletColor = Color(0xFF64748B)
            // Front line-tie eyelet
            drawCircle(Color(0xFFE2E8F0), radius = 4.5.dp.toPx(), center = Offset(centerX - halfL - 6.dp.toPx(), centerY))
            drawCircle(eyeletColor, radius = 4.5.dp.toPx(), center = Offset(centerX - halfL - 6.dp.toPx(), centerY), style = Stroke(width = 1.5.dp.toPx()))
            drawCircle(Color(0xFF0F172A), radius = 1.5.dp.toPx(), center = Offset(centerX - halfL - 6.dp.toPx(), centerY))

            // Rear hook-tie eyelet
            drawCircle(Color(0xFFE2E8F0), radius = 4.5.dp.toPx(), center = Offset(centerX + halfL + 6.dp.toPx(), centerY))
            drawCircle(eyeletColor, radius = 4.5.dp.toPx(), center = Offset(centerX + halfL + 6.dp.toPx(), centerY), style = Stroke(width = 1.5.dp.toPx()))
            drawCircle(Color(0xFF0F172A), radius = 1.5.dp.toPx(), center = Offset(centerX + halfL + 6.dp.toPx(), centerY))

            // 3D Eye
            val eyeX = centerX - halfL + 18.dp.toPx() * scaleLength
            val eyeY = centerY - 4.dp.toPx()
            drawCircle(Color.White, radius = 3.8.dp.toPx(), center = Offset(eyeX, eyeY))
            drawCircle(Color(0xFF0F172A), radius = 3.8.dp.toPx(), center = Offset(eyeX, eyeY), style = Stroke(width = 1.dp.toPx()))
            drawCircle(Color(0xFF0F172A), radius = 1.8.dp.toPx(), center = Offset(eyeX + 0.8.dp.toPx(), eyeY))

            // 4. Dimension Lines & Callouts
            // Length Dimension Line (Top)
            val dimY = centerY - halfW - 16.dp.toPx()
            val dimX1 = centerX - halfL - 8.dp.toPx()
            val dimX2 = centerX + halfL + 8.dp.toPx()
            val dimColor = Color(0xFF0F172A)
            val dimStroke = 1.dp.toPx()

            // Extension lines
            drawLine(dimColor, Offset(dimX1, centerY), Offset(dimX1, dimY - 4.dp.toPx()), strokeWidth = dimStroke)
            drawLine(dimColor, Offset(dimX2, centerY), Offset(dimX2, dimY - 4.dp.toPx()), strokeWidth = dimStroke)
            // Dimension line
            drawLine(dimColor, Offset(dimX1, dimY), Offset(dimX2, dimY), strokeWidth = dimStroke)
            // Arrows
            drawArrow(this, Offset(dimX1, dimY), isPointingRight = false, color = dimColor)
            drawArrow(this, Offset(dimX2, dimY), isPointingRight = true, color = dimColor)

            // Length text
            val lengthText = textMeasurer.measure(
                AnnotatedString("${config.lengthMm.toInt()} mm"),
                style = TextStyle(
                    color = dimColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(
                textLayoutResult = lengthText,
                topLeft = Offset((dimX1 + dimX2) / 2f - lengthText.size.width / 2f, dimY - lengthText.size.height - 2.dp.toPx())
            )

            // Width Dimension (Right)
            val vDimX = centerX + halfL + 24.dp.toPx()
            val vDimY1 = centerY - halfW
            val vDimY2 = centerY + halfW
            drawLine(dimColor, Offset(centerX, vDimY1), Offset(vDimX + 4.dp.toPx(), vDimY1), strokeWidth = dimStroke)
            drawLine(dimColor, Offset(centerX, vDimY2), Offset(vDimX + 4.dp.toPx(), vDimY2), strokeWidth = dimStroke)
            drawLine(dimColor, Offset(vDimX, vDimY1), Offset(vDimX, vDimY2), strokeWidth = dimStroke)
            drawVerticalArrow(this, Offset(vDimX, vDimY1), isPointingDown = false, color = dimColor)
            drawVerticalArrow(this, Offset(vDimX, vDimY2), isPointingDown = true, color = dimColor)

            val widthText = textMeasurer.measure(
                AnnotatedString("${config.widthMm.toInt()} mm"),
                style = TextStyle(
                    color = dimColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(
                textLayoutResult = widthText,
                topLeft = Offset(vDimX + 6.dp.toPx(), (vDimY1 + vDimY2) / 2f - widthText.size.height / 2f)
            )

            // Title Tag Top Left
            val tagText = textMeasurer.measure(
                AnnotatedString("CAD PROJECTION • MODEL ${config.modelNumber}"),
                style = TextStyle(
                    color = Color(0xFF0284C7),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(tagText, topLeft = Offset(4.dp.toPx(), 4.dp.toPx()))

            // Weight & Material Badge Bottom Left
            val badgeText = textMeasurer.measure(
                AnnotatedString("MASS: ${config.weightGrams.toInt()}g | MAT: ${config.material}"),
                style = TextStyle(
                    color = Color(0xFF475569),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            )
            drawText(badgeText, topLeft = Offset(4.dp.toPx(), canvasHeight - 20.dp.toPx()))
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun RodEngineeringCanvas(
    config: ProductConfiguration,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color(0xFFFCFDFD), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Grid
            val gridSpacing = 20.dp.toPx()
            val gridColor = Color(0x180284C7)
            var x = 0f
            while (x <= canvasWidth) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, canvasHeight), strokeWidth = 1f)
                x += gridSpacing
            }
            var y = 0f
            while (y <= canvasHeight) {
                drawLine(gridColor, Offset(0f, y), Offset(canvasWidth, y), strokeWidth = 1f)
                y += gridSpacing
            }

            val startX = 24.dp.toPx()
            val endX = canvasWidth - 36.dp.toPx()
            val centerY = canvasHeight * 0.52f

            // Red centerline
            val centerLineColor = Color(0xFFEF4444)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 4f, 2f, 4f), 0f)
            drawLine(
                color = centerLineColor,
                start = Offset(startX - 12.dp.toPx(), centerY),
                end = Offset(endX + 18.dp.toPx(), centerY),
                strokeWidth = 1.2f,
                pathEffect = dashEffect
            )

            val buttLength = (endX - startX) * 0.22f
            val buttRadius = (config.widthMm * 0.65f).coerceIn(5.dp.toPx(), 11.dp.toPx())

            // 1. EVA / Cork Split Grip Handle
            val handleColor = Color(0xFF1E293B)
            // Rear grip
            drawRoundRect(
                color = handleColor,
                topLeft = Offset(startX, centerY - buttRadius),
                size = Size(buttLength * 0.4f, buttRadius * 2),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            // Reel seat collar
            drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(startX + buttLength * 0.45f, centerY - buttRadius * 1.15f),
                size = Size(buttLength * 0.32f, buttRadius * 2.3f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            // Fore grip
            drawRoundRect(
                color = handleColor,
                topLeft = Offset(startX + buttLength * 0.8f, centerY - buttRadius * 0.85f),
                size = Size(buttLength * 0.2f, buttRadius * 1.7f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // 2. Carbon Blank Taper
            val blankPath = Path().apply {
                moveTo(startX + buttLength, centerY - buttRadius * 0.6f)
                lineTo(endX, centerY - 1.2.dp.toPx())
                lineTo(endX, centerY + 1.2.dp.toPx())
                lineTo(startX + buttLength, centerY + buttRadius * 0.6f)
                close()
            }
            drawPath(blankPath, color = Color(0xFF0F172A))

            // Cross-weave pattern on base of blank
            val weaveColor = Color(0xFF0284C7)
            var wx = startX + buttLength
            while (wx <= startX + buttLength + 70.dp.toPx()) {
                drawLine(weaveColor, Offset(wx, centerY - 3.dp.toPx()), Offset(wx + 4.dp.toPx(), centerY + 3.dp.toPx()), strokeWidth = 1f)
                drawLine(weaveColor, Offset(wx + 4.dp.toPx(), centerY - 3.dp.toPx()), Offset(wx, centerY + 3.dp.toPx()), strokeWidth = 1f)
                wx += 8.dp.toPx()
            }

            // 3. Graduated Guides
            val guideCount = 7
            val guideColor = Color(0xFFEA580C)
            val blankLength = endX - (startX + buttLength)
            for (i in 1..guideCount) {
                val ratio = (i.toFloat() / (guideCount + 1)).let { it * it }
                val gx = startX + buttLength + blankLength * ratio
                val gHeight = (14.dp.toPx() - i * 1.5.dp.toPx()).coerceAtLeast(3.5.dp.toPx())
                drawLine(guideColor, Offset(gx, centerY - 1.5.dp.toPx()), Offset(gx - 2.dp.toPx(), centerY - 1.5.dp.toPx() - gHeight), strokeWidth = 1.2.dp.toPx())
                drawCircle(guideColor, radius = (gHeight * 0.35f).coerceAtLeast(1.2.dp.toPx()), center = Offset(gx - 2.dp.toPx(), centerY - 1.5.dp.toPx() - gHeight), style = Stroke(width = 1.dp.toPx()))
            }
            // Tip top
            drawCircle(guideColor, radius = 2.5.dp.toPx(), center = Offset(endX + 1.5.dp.toPx(), centerY))

            // 4. Dimension lines
            val dimColor = Color(0xFF0F172A)
            val dimStroke = 1.dp.toPx()

            // Overall Length
            val dimY = centerY - 35.dp.toPx()
            drawLine(dimColor, Offset(startX, centerY), Offset(startX, dimY - 4.dp.toPx()), strokeWidth = dimStroke)
            drawLine(dimColor, Offset(endX, centerY), Offset(endX, dimY - 4.dp.toPx()), strokeWidth = dimStroke)
            drawLine(dimColor, Offset(startX, dimY), Offset(endX, dimY), strokeWidth = dimStroke)
            drawArrow(this, Offset(startX, dimY), isPointingRight = false, color = dimColor)
            drawArrow(this, Offset(endX, dimY), isPointingRight = true, color = dimColor)

            val lengthText = textMeasurer.measure(
                AnnotatedString("TOTAL LENGTH: ${config.lengthMm.toInt()} mm (${String.format(java.util.Locale.US, "%.2f", config.lengthMm / 1000f)}m)"),
                style = TextStyle(
                    color = dimColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(lengthText, topLeft = Offset((startX + endX) / 2f - lengthText.size.width / 2f, dimY - lengthText.size.height - 2.dp.toPx()))

            // Handle Length
            val hDimY = centerY + 28.dp.toPx()
            drawLine(dimColor, Offset(startX, centerY), Offset(startX, hDimY + 4.dp.toPx()), strokeWidth = dimStroke)
            drawLine(dimColor, Offset(startX + buttLength, centerY), Offset(startX + buttLength, hDimY + 4.dp.toPx()), strokeWidth = dimStroke)
            drawLine(dimColor, Offset(startX, hDimY), Offset(startX + buttLength, hDimY), strokeWidth = dimStroke)
            drawArrow(this, Offset(startX, hDimY), isPointingRight = false, color = dimColor)
            drawArrow(this, Offset(startX + buttLength, hDimY), isPointingRight = true, color = dimColor)

            val handleText = textMeasurer.measure(
                AnnotatedString("HANDLE: ${config.handleLengthMm.toInt()}mm"),
                style = TextStyle(
                    color = dimColor,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(handleText, topLeft = Offset(startX + buttLength / 2f - handleText.size.width / 2f, hDimY + 3.dp.toPx()))

            // Title & Info
            val tagText = textMeasurer.measure(
                AnnotatedString("ROD CAD ARCHITECTURE • ${config.rodType}"),
                style = TextStyle(
                    color = Color(0xFF0284C7),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(tagText, topLeft = Offset(4.dp.toPx(), 4.dp.toPx()))

            val badgeText = textMeasurer.measure(
                AnnotatedString("POWER: ${config.power} | ACTION: ${config.action} | SECTIONS: ${config.sections}pc"),
                style = TextStyle(
                    color = Color(0xFF475569),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            )
            drawText(badgeText, topLeft = Offset(4.dp.toPx(), canvasHeight - 20.dp.toPx()))
        }
    }
}

private fun drawArrow(scope: DrawScope, point: Offset, isPointingRight: Boolean, color: Color) {
    val dir = if (isPointingRight) -1 else 1
    val arrowPath = Path().apply {
        moveTo(point.x, point.y)
        lineTo(point.x + dir * 5.dp.toPx(scope), point.y - 2.5.dp.toPx(scope))
        lineTo(point.x + dir * 5.dp.toPx(scope), point.y + 2.5.dp.toPx(scope))
        close()
    }
    scope.drawPath(arrowPath, color = color)
}

private fun drawVerticalArrow(scope: DrawScope, point: Offset, isPointingDown: Boolean, color: Color) {
    val dir = if (isPointingDown) -1 else 1
    val arrowPath = Path().apply {
        moveTo(point.x, point.y)
        lineTo(point.x - 2.5.dp.toPx(scope), point.y + dir * 5.dp.toPx(scope))
        lineTo(point.x + 2.5.dp.toPx(scope), point.y + dir * 5.dp.toPx(scope))
        close()
    }
    scope.drawPath(arrowPath, color = color)
}

private fun androidx.compose.ui.unit.Dp.toPx(scope: DrawScope): Float = with(scope) { this@toPx.toPx() }
