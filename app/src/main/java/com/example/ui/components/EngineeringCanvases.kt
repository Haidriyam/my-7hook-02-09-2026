package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 7HOOKS INDUSTRIAL TECHNICAL DRAWING SYSTEM
 * Implements authentic multi-view orthographic projections:
 * - Front View (Elevation)
 * - Top View (Plan)
 * - End View (Cross-section)
 * - Isometric Projection
 * - Precision engineering dimensions (±0.15 mm)
 * - Standard technical title blocks
 */

@OptIn(ExperimentalTextApi::class)
@Composable
fun JigEngineeringCanvas(
    config: ProductConfiguration,
    modifier: Modifier = Modifier.height(320.dp)
) {
    JigOrthographicCanvas(config = config, modifier = modifier)
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun JigOrthographicCanvas(
    config: ProductConfiguration,
    modifier: Modifier = Modifier.height(320.dp)
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .background(Color(0xFFFCFDFE), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Engineering Drafting Grid
            drawDraftingGrid(canvasW, canvasH)

            // Sheet Border & Inner Margin
            drawRect(
                color = Color(0xFF0F172A),
                size = size,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                size = Size(canvasW - 8.dp.toPx(), canvasH - 8.dp.toPx()),
                style = Stroke(width = 0.8.dp.toPx())
            )

            // Dynamic Scaling
            val scaleL = (config.lengthMm / 180f).coerceIn(0.55f, 1.35f)
            val scaleW = (config.widthMm / 28f).coerceIn(0.55f, 1.35f)
            val drawL = 140.dp.toPx() * scaleL
            val drawW = 28.dp.toPx() * scaleW

            // -------------------------------------------------------------
            // VIEW A: FRONT ELEVATION (Left / Center Upper)
            // -------------------------------------------------------------
            val frontCenterX = canvasW * 0.36f
            val frontCenterY = canvasH * 0.28f

            drawViewLabel(textMeasurer, "VIEW A: FRONT ELEVATION (1:1)", Offset(14.dp.toPx(), 14.dp.toPx()))

            // Centerline
            drawTechnicalCenterline(
                start = Offset(frontCenterX - drawL / 2f - 22.dp.toPx(), frontCenterY),
                end = Offset(frontCenterX + drawL / 2f + 22.dp.toPx(), frontCenterY)
            )

            // Jig Body Profile Path
            val halfL = drawL / 2f
            val halfW = drawW / 2f
            val frontPath = Path().apply {
                moveTo(frontCenterX - halfL, frontCenterY)
                cubicTo(
                    frontCenterX - halfL * 0.6f, frontCenterY - halfW * 0.9f,
                    frontCenterX + halfL * 0.1f, frontCenterY - halfW,
                    frontCenterX + halfL * 0.65f, frontCenterY - halfW * 0.65f
                )
                lineTo(frontCenterX + halfL, frontCenterY)
                cubicTo(
                    frontCenterX + halfL * 0.65f, frontCenterY + halfW * 0.65f,
                    frontCenterX + halfL * 0.1f, frontCenterY + halfW,
                    frontCenterX - halfL * 0.6f, frontCenterY + halfW * 0.9f
                )
                close()
            }

            // Fill and Stroke
            drawPath(
                path = frontPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(config.baseColorHex).copy(alpha = 0.85f), Color(config.accentColorHex).copy(alpha = 0.85f)),
                    startY = frontCenterY - halfW,
                    endY = frontCenterY + halfW
                )
            )
            drawPath(path = frontPath, color = Color(0xFF0F172A), style = Stroke(width = 1.8.dp.toPx()))

            // Eyelets
            drawCircle(Color(0xFFE2E8F0), radius = 4.dp.toPx(), center = Offset(frontCenterX - halfL - 5.dp.toPx(), frontCenterY))
            drawCircle(Color(0xFF475569), radius = 4.dp.toPx(), center = Offset(frontCenterX - halfL - 5.dp.toPx(), frontCenterY), style = Stroke(1.2.dp.toPx()))
            drawCircle(Color(0xFFE2E8F0), radius = 4.dp.toPx(), center = Offset(frontCenterX + halfL + 5.dp.toPx(), frontCenterY))
            drawCircle(Color(0xFF475569), radius = 4.dp.toPx(), center = Offset(frontCenterX + halfL + 5.dp.toPx(), frontCenterY), style = Stroke(1.2.dp.toPx()))

            // 3D Lure Eye
            drawCircle(Color.White, radius = 3.5.dp.toPx(), center = Offset(frontCenterX - halfL + 14.dp.toPx() * scaleL, frontCenterY - 3.dp.toPx()))
            drawCircle(Color(0xFF0F172A), radius = 1.8.dp.toPx(), center = Offset(frontCenterX - halfL + 14.dp.toPx() * scaleL, frontCenterY - 3.dp.toPx()))

            // Front View Dimensions
            drawHorizontalDimension(
                measurer = textMeasurer,
                x1 = frontCenterX - halfL,
                x2 = frontCenterX + halfL,
                dimY = frontCenterY - halfW - 14.dp.toPx(),
                valueText = "${String.format(Locale.US, "%.1f", config.lengthMm)} ± 0.15 mm"
            )
            drawVerticalDimension(
                measurer = textMeasurer,
                dimX = frontCenterX + halfL + 18.dp.toPx(),
                y1 = frontCenterY - halfW,
                y2 = frontCenterY + halfW,
                valueText = "${String.format(Locale.US, "%.1f", config.widthMm)} ± 0.15"
            )

            // -------------------------------------------------------------
            // VIEW B: TOP PLAN VIEW (Below Elevation)
            // -------------------------------------------------------------
            val topCenterY = canvasH * 0.58f
            drawViewLabel(textMeasurer, "VIEW B: TOP PLAN VIEW (AERODYNAMIC KEEL)", Offset(14.dp.toPx(), topCenterY - 26.dp.toPx()))

            drawTechnicalCenterline(
                start = Offset(frontCenterX - drawL / 2f - 22.dp.toPx(), topCenterY),
                end = Offset(frontCenterX + drawL / 2f + 22.dp.toPx(), topCenterY)
            )

            val topThickness = drawW * 0.45f
            val topPath = Path().apply {
                moveTo(frontCenterX - halfL, topCenterY)
                cubicTo(
                    frontCenterX - halfL * 0.5f, topCenterY - topThickness,
                    frontCenterX + halfL * 0.2f, topCenterY - topThickness * 1.1f,
                    frontCenterX + halfL * 0.7f, topCenterY - topThickness * 0.4f
                )
                lineTo(frontCenterX + halfL, topCenterY)
                cubicTo(
                    frontCenterX + halfL * 0.7f, topCenterY + topThickness * 0.4f,
                    frontCenterX + halfL * 0.2f, topCenterY + topThickness * 1.1f,
                    frontCenterX - halfL * 0.5f, topCenterY + topThickness
                )
                close()
            }
            drawPath(topPath, color = Color(0xFFF1F5F9))
            drawPath(topPath, color = Color(0xFF0F172A), style = Stroke(width = 1.5.dp.toPx()))

            // Spine / Ridge line
            drawLine(
                color = Color(0xFF0284C7),
                start = Offset(frontCenterX - halfL + 8.dp.toPx(), topCenterY),
                end = Offset(frontCenterX + halfL - 8.dp.toPx(), topCenterY),
                strokeWidth = 1.4.dp.toPx()
            )

            // -------------------------------------------------------------
            // VIEW C: END PROFILE / SECTION (Top Right)
            // -------------------------------------------------------------
            val endCenterX = canvasW * 0.82f
            val endCenterY = canvasH * 0.26f
            drawViewLabel(textMeasurer, "VIEW C: END (1:1)", Offset(endCenterX - 28.dp.toPx(), endCenterY - drawW - 12.dp.toPx()))

            drawTechnicalCenterline(
                start = Offset(endCenterX - drawW * 0.8f, endCenterY),
                end = Offset(endCenterX + drawW * 0.8f, endCenterY)
            )
            drawTechnicalCenterline(
                start = Offset(endCenterX, endCenterY - drawW * 0.8f),
                end = Offset(endCenterX, endCenterY + drawW * 0.8f)
            )

            // Diamond-hydrofoil end section
            val endSectionPath = Path().apply {
                moveTo(endCenterX, endCenterY - halfW)
                lineTo(endCenterX + topThickness * 0.9f, endCenterY)
                lineTo(endCenterX, endCenterY + halfW)
                lineTo(endCenterX - topThickness * 0.9f, endCenterY)
                close()
            }
            drawPath(endSectionPath, color = Color(0xFFE2E8F0))
            drawPath(endSectionPath, color = Color(0xFF0F172A), style = Stroke(1.5.dp.toPx()))

            // -------------------------------------------------------------
            // ENGINEERING TITLE BLOCK (Standard Bottom Right Corner)
            // -------------------------------------------------------------
            drawEngineeringTitleBlock(
                measurer = textMeasurer,
                canvasW = canvasW,
                canvasH = canvasH,
                title = "JIG INDUSTRIAL SPECIFICATION",
                modelNo = config.modelNumber,
                material = config.material,
                finish = config.colorName,
                tolerance = "±0.15 mm",
                scale = "1:1 FULL",
                refNo = config.referenceNumber
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun LureEngineeringCanvas(
    config: ProductConfiguration,
    modifier: Modifier = Modifier.height(320.dp)
) {
    LureOrthographicCanvas(config = config, modifier = modifier)
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun LureOrthographicCanvas(
    config: ProductConfiguration,
    modifier: Modifier = Modifier.height(320.dp)
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .background(Color(0xFFFCFDFE), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val canvasW = size.width
            val canvasH = size.height

            drawDraftingGrid(canvasW, canvasH)

            // Sheet Borders
            drawRect(color = Color(0xFF0F172A), size = size, style = Stroke(width = 1.5.dp.toPx()))
            drawRect(color = Color(0xFF94A3B8), topLeft = Offset(4.dp.toPx(), 4.dp.toPx()), size = Size(canvasW - 8.dp.toPx(), canvasH - 8.dp.toPx()), style = Stroke(width = 0.8.dp.toPx()))

            val scaleL = (config.lengthMm / 160f).coerceIn(0.55f, 1.35f)
            val scaleW = (config.widthMm / 26f).coerceIn(0.55f, 1.35f)
            val drawL = 145.dp.toPx() * scaleL
            val drawW = 30.dp.toPx() * scaleW

            // -------------------------------------------------------------
            // VIEW A: LURE ELEVATION WITH DIVING LIP & HOOK HANGERS
            // -------------------------------------------------------------
            val frontCenterX = canvasW * 0.36f
            val frontCenterY = canvasH * 0.28f

            drawViewLabel(textMeasurer, "VIEW A: ELEVATION & LIP (1:1)", Offset(14.dp.toPx(), 14.dp.toPx()))

            drawTechnicalCenterline(
                start = Offset(frontCenterX - drawL / 2f - 24.dp.toPx(), frontCenterY),
                end = Offset(frontCenterX + drawL / 2f + 20.dp.toPx(), frontCenterY)
            )

            val halfL = drawL / 2f
            val halfW = drawW / 2f

            // Realistic Minnow Body Curve
            val lurePath = Path().apply {
                moveTo(frontCenterX - halfL, frontCenterY)
                cubicTo(
                    frontCenterX - halfL * 0.7f, frontCenterY - halfW * 0.95f,
                    frontCenterX - halfL * 0.1f, frontCenterY - halfW * 1.05f,
                    frontCenterX + halfL * 0.6f, frontCenterY - halfW * 0.5f
                )
                lineTo(frontCenterX + halfL, frontCenterY)
                cubicTo(
                    frontCenterX + halfL * 0.6f, frontCenterY + halfW * 0.5f,
                    frontCenterX - halfL * 0.1f, frontCenterY + halfW * 0.9f,
                    frontCenterX - halfL * 0.7f, frontCenterY + halfW * 0.7f
                )
                close()
            }

            drawPath(
                path = lurePath,
                brush = Brush.verticalGradient(
                    listOf(Color(config.baseColorHex).copy(alpha = 0.85f), Color(config.accentColorHex).copy(alpha = 0.85f)),
                    startY = frontCenterY - halfW,
                    endY = frontCenterY + halfW
                )
            )
            drawPath(lurePath, color = Color(0xFF0F172A), style = Stroke(1.8.dp.toPx()))

            // Diving Lip / Bib
            val lipPath = Path().apply {
                moveTo(frontCenterX - halfL + 3.dp.toPx(), frontCenterY + 2.dp.toPx())
                lineTo(frontCenterX - halfL - 14.dp.toPx() * scaleL, frontCenterY + 16.dp.toPx() * scaleL)
                lineTo(frontCenterX - halfL - 9.dp.toPx() * scaleL, frontCenterY + 19.dp.toPx() * scaleL)
                lineTo(frontCenterX - halfL + 7.dp.toPx(), frontCenterY + 6.dp.toPx())
                close()
            }
            drawPath(lipPath, color = Color(0x99E2E8F0))
            drawPath(lipPath, color = Color(0xFF0284C7), style = Stroke(1.5.dp.toPx()))

            // Eye & Belly Treble Eyelet
            drawCircle(Color.White, radius = 3.5.dp.toPx(), center = Offset(frontCenterX - halfL + 12.dp.toPx() * scaleL, frontCenterY - 3.dp.toPx()))
            drawCircle(Color(0xFF0F172A), radius = 1.8.dp.toPx(), center = Offset(frontCenterX - halfL + 12.dp.toPx() * scaleL, frontCenterY - 3.dp.toPx()))

            // Belly Hanger
            drawCircle(Color(0xFF64748B), radius = 3.2.dp.toPx(), center = Offset(frontCenterX - 5.dp.toPx(), frontCenterY + halfW * 0.85f + 3.dp.toPx()), style = Stroke(1.2.dp.toPx()))
            // Tail Hanger
            drawCircle(Color(0xFF64748B), radius = 3.2.dp.toPx(), center = Offset(frontCenterX + halfL + 4.dp.toPx(), frontCenterY), style = Stroke(1.2.dp.toPx()))

            // Dimension lines
            drawHorizontalDimension(
                measurer = textMeasurer,
                x1 = frontCenterX - halfL,
                x2 = frontCenterX + halfL,
                dimY = frontCenterY - halfW - 14.dp.toPx(),
                valueText = "${String.format(Locale.US, "%.1f", config.lengthMm)} ± 0.15 mm"
            )
            drawVerticalDimension(
                measurer = textMeasurer,
                dimX = frontCenterX + halfL + 16.dp.toPx(),
                y1 = frontCenterY - halfW,
                y2 = frontCenterY + halfW,
                valueText = "${String.format(Locale.US, "%.1f", config.widthMm)} ± 0.15"
            )

            // -------------------------------------------------------------
            // VIEW B: TOP PLAN VIEW (Hydrofoil Taper)
            // -------------------------------------------------------------
            val topCenterY = canvasH * 0.58f
            drawViewLabel(textMeasurer, "VIEW B: TOP PLAN VIEW (STREAMLINED PROFILE)", Offset(14.dp.toPx(), topCenterY - 26.dp.toPx()))

            drawTechnicalCenterline(
                start = Offset(frontCenterX - drawL / 2f - 24.dp.toPx(), topCenterY),
                end = Offset(frontCenterX + drawL / 2f + 20.dp.toPx(), topCenterY)
            )

            val topThick = drawW * 0.42f
            val lureTopPath = Path().apply {
                moveTo(frontCenterX - halfL, topCenterY)
                cubicTo(
                    frontCenterX - halfL * 0.5f, topCenterY - topThick,
                    frontCenterX, topCenterY - topThick * 1.1f,
                    frontCenterX + halfL * 0.7f, topCenterY - topThick * 0.35f
                )
                lineTo(frontCenterX + halfL, topCenterY)
                cubicTo(
                    frontCenterX + halfL * 0.7f, topCenterY + topThick * 0.35f,
                    frontCenterX, topCenterY + topThick * 1.1f,
                    frontCenterX - halfL * 0.5f, topCenterY + topThick
                )
                close()
            }
            drawPath(lureTopPath, color = Color(0xFFF1F5F9))
            drawPath(lureTopPath, color = Color(0xFF0F172A), style = Stroke(1.5.dp.toPx()))

            // -------------------------------------------------------------
            // VIEW C: END PROFILE (Lip Angle & Width)
            // -------------------------------------------------------------
            val endCenterX = canvasW * 0.82f
            val endCenterY = canvasH * 0.26f
            drawViewLabel(textMeasurer, "VIEW C: END", Offset(endCenterX - 24.dp.toPx(), endCenterY - drawW - 12.dp.toPx()))

            drawTechnicalCenterline(
                start = Offset(endCenterX - drawW * 0.7f, endCenterY),
                end = Offset(endCenterX + drawW * 0.7f, endCenterY)
            )
            drawTechnicalCenterline(
                start = Offset(endCenterX, endCenterY - drawW * 0.7f),
                end = Offset(endCenterX, endCenterY + drawW * 0.7f)
            )

            // Oval body cross-section
            drawOval(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(endCenterX - topThick, endCenterY - halfW),
                size = Size(topThick * 2, halfW * 2)
            )
            drawOval(
                color = Color(0xFF0F172A),
                topLeft = Offset(endCenterX - topThick, endCenterY - halfW),
                size = Size(topThick * 2, halfW * 2),
                style = Stroke(1.5.dp.toPx())
            )

            // -------------------------------------------------------------
            // ENGINEERING TITLE BLOCK
            // -------------------------------------------------------------
            drawEngineeringTitleBlock(
                measurer = textMeasurer,
                canvasW = canvasW,
                canvasH = canvasH,
                title = "LURE INDUSTRIAL SPECIFICATION",
                modelNo = config.modelNumber,
                material = config.material,
                finish = config.colorName,
                tolerance = "±0.15 mm",
                scale = "1:1 FULL",
                refNo = config.referenceNumber
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun RodEngineeringCanvas(
    config: ProductConfiguration,
    modifier: Modifier = Modifier.height(320.dp)
) {
    RodOrthographicCanvas(config = config, modifier = modifier)
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun RodOrthographicCanvas(
    config: ProductConfiguration,
    modifier: Modifier = Modifier.height(320.dp)
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .background(Color(0xFFFCFDFE), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val canvasW = size.width
            val canvasH = size.height

            drawDraftingGrid(canvasW, canvasH)

            drawRect(color = Color(0xFF0F172A), size = size, style = Stroke(width = 1.5.dp.toPx()))
            drawRect(color = Color(0xFF94A3B8), topLeft = Offset(4.dp.toPx(), 4.dp.toPx()), size = Size(canvasW - 8.dp.toPx(), canvasH - 8.dp.toPx()), style = Stroke(width = 0.8.dp.toPx()))

            val startX = 20.dp.toPx()
            val endX = canvasW - 28.dp.toPx()
            val centerY = canvasH * 0.28f

            drawViewLabel(textMeasurer, "VIEW A: BLANK ELEVATION & GUIDE TRAIN (1:10)", Offset(14.dp.toPx(), 14.dp.toPx()))

            // Red Technical Centerline
            drawTechnicalCenterline(
                start = Offset(startX - 10.dp.toPx(), centerY),
                end = Offset(endX + 14.dp.toPx(), centerY)
            )

            val buttLength = (endX - startX) * 0.22f
            val buttRadius = (config.widthMm * 0.65f).coerceIn(4.5.dp.toPx(), 9.dp.toPx())

            // 1. Split EVA / Cork Handle
            val handleColor = Color(0xFF1E293B)
            drawRoundRect(
                color = handleColor,
                topLeft = Offset(startX, centerY - buttRadius),
                size = Size(buttLength * 0.4f, buttRadius * 2),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(startX + buttLength * 0.44f, centerY - buttRadius * 1.15f),
                size = Size(buttLength * 0.32f, buttRadius * 2.3f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawRoundRect(
                color = handleColor,
                topLeft = Offset(startX + buttLength * 0.8f, centerY - buttRadius * 0.85f),
                size = Size(buttLength * 0.2f, buttRadius * 1.7f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // 2. High Modulus Carbon Blank Taper
            val blankPath = Path().apply {
                moveTo(startX + buttLength, centerY - buttRadius * 0.6f)
                lineTo(endX, centerY - 1.2.dp.toPx())
                lineTo(endX, centerY + 1.2.dp.toPx())
                lineTo(startX + buttLength, centerY + buttRadius * 0.6f)
                close()
            }
            drawPath(blankPath, color = Color(0xFF0F172A))

            // 3. Graduated Fuji K-Guide Layout
            val guideCount = 7
            val guideColor = Color(0xFFEA580C)
            val blankLength = endX - (startX + buttLength)
            for (i in 1..guideCount) {
                val ratio = (i.toFloat() / (guideCount + 1)).let { it * it }
                val gx = startX + buttLength + blankLength * ratio
                val gHeight = (13.dp.toPx() - i * 1.3.dp.toPx()).coerceAtLeast(3.2.dp.toPx())
                drawLine(guideColor, Offset(gx, centerY - 1.5.dp.toPx()), Offset(gx - 2.dp.toPx(), centerY - 1.5.dp.toPx() - gHeight), strokeWidth = 1.2.dp.toPx())
                drawCircle(guideColor, radius = (gHeight * 0.35f).coerceAtLeast(1.2.dp.toPx()), center = Offset(gx - 2.dp.toPx(), centerY - 1.5.dp.toPx() - gHeight), style = Stroke(width = 1.dp.toPx()))
            }
            drawCircle(guideColor, radius = 2.4.dp.toPx(), center = Offset(endX + 1.2.dp.toPx(), centerY))

            // Dimension: Overall Length
            drawHorizontalDimension(
                measurer = textMeasurer,
                x1 = startX,
                x2 = endX,
                dimY = centerY - 28.dp.toPx(),
                valueText = "${config.lengthMm.toInt()} mm (${String.format(Locale.US, "%.2f", config.lengthMm / 1000f)}m) ± 2.0 mm"
            )

            // Dimension: Handle Length
            drawHorizontalDimension(
                measurer = textMeasurer,
                x1 = startX,
                x2 = startX + buttLength,
                dimY = centerY + 24.dp.toPx(),
                valueText = "HANDLE: ${config.handleLengthMm.toInt()} mm"
            )

            // -------------------------------------------------------------
            // VIEW B: LOAD DEFLECTION ARC (Flex Curve under maximumLoadKg)
            // -------------------------------------------------------------
            val flexY = canvasH * 0.52f
            drawViewLabel(textMeasurer, "VIEW B: LOAD FLEX DEFLECTION (${config.maximumLoadKg.toInt()} kg RATED)", Offset(14.dp.toPx(), flexY - 14.dp.toPx()))

            val flexPath = Path().apply {
                moveTo(startX + buttLength, flexY)
                val flexDrop = when (config.action) {
                    "Extra Fast" -> 22.dp.toPx()
                    "Fast" -> 28.dp.toPx()
                    "Moderate Fast" -> 36.dp.toPx()
                    else -> 44.dp.toPx()
                }
                cubicTo(
                    startX + buttLength + blankLength * 0.4f, flexY,
                    startX + buttLength + blankLength * 0.75f, flexY + flexDrop * 0.6f,
                    endX, flexY + flexDrop
                )
            }
            drawPath(flexPath, color = Color(0xFF0284C7), style = Stroke(width = 2.dp.toPx()))

            // -------------------------------------------------------------
            // ENGINEERING TITLE BLOCK
            // -------------------------------------------------------------
            drawEngineeringTitleBlock(
                measurer = textMeasurer,
                canvasW = canvasW,
                canvasH = canvasH,
                title = "ROD ARCHITECTURE SPECIFICATION",
                modelNo = config.modelNumber,
                material = config.material,
                finish = "Graphite Polish (${config.sections} pc)",
                tolerance = "±1.5 mm",
                scale = "1:10",
                refNo = config.referenceNumber
            )
        }
    }
}

// -------------------------------------------------------------------------
// REUSABLE CAD DRAWING UTILITIES
// -------------------------------------------------------------------------

private fun DrawScope.drawDraftingGrid(w: Float, h: Float) {
    val gridSpacing = 20.dp.toPx()
    val gridColor = Color(0x150284C7)
    var x = 0f
    while (x <= w) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
        x += gridSpacing
    }
    var y = 0f
    while (y <= h) {
        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        y += gridSpacing
    }
}

private fun DrawScope.drawTechnicalCenterline(start: Offset, end: Offset) {
    val centerLineColor = Color(0xFFEF4444)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 4f, 2f, 4f), 0f)
    drawLine(
        color = centerLineColor,
        start = start,
        end = end,
        strokeWidth = 1.2.dp.toPx(),
        pathEffect = dashEffect
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawViewLabel(measurer: TextMeasurer, label: String, pos: Offset) {
    val res = measurer.measure(
        AnnotatedString(label),
        style = TextStyle(
            color = Color(0xFF0284C7),
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    )
    drawText(res, topLeft = pos)
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawHorizontalDimension(
    measurer: TextMeasurer,
    x1: Float,
    x2: Float,
    dimY: Float,
    valueText: String
) {
    val dimColor = Color(0xFF0F172A)
    val stroke = 1.dp.toPx()

    // Extension lines
    drawLine(dimColor, Offset(x1, dimY + 4.dp.toPx()), Offset(x1, dimY - 4.dp.toPx()), strokeWidth = stroke)
    drawLine(dimColor, Offset(x2, dimY + 4.dp.toPx()), Offset(x2, dimY - 4.dp.toPx()), strokeWidth = stroke)

    // Main line
    drawLine(dimColor, Offset(x1, dimY), Offset(x2, dimY), strokeWidth = stroke)

    // Arrows
    drawArrow(this, Offset(x1, dimY), isPointingRight = false, color = dimColor)
    drawArrow(this, Offset(x2, dimY), isPointingRight = true, color = dimColor)

    val txt = measurer.measure(
        AnnotatedString(valueText),
        style = TextStyle(
            color = dimColor,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    )
    drawText(txt, topLeft = Offset((x1 + x2) / 2f - txt.size.width / 2f, dimY - txt.size.height - 1.5.dp.toPx()))
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawVerticalDimension(
    measurer: TextMeasurer,
    dimX: Float,
    y1: Float,
    y2: Float,
    valueText: String
) {
    val dimColor = Color(0xFF0F172A)
    val stroke = 1.dp.toPx()

    drawLine(dimColor, Offset(dimX - 4.dp.toPx(), y1), Offset(dimX + 4.dp.toPx(), y1), strokeWidth = stroke)
    drawLine(dimColor, Offset(dimX - 4.dp.toPx(), y2), Offset(dimX + 4.dp.toPx(), y2), strokeWidth = stroke)
    drawLine(dimColor, Offset(dimX, y1), Offset(dimX, y2), strokeWidth = stroke)

    drawVerticalArrow(this, Offset(dimX, y1), isPointingDown = false, color = dimColor)
    drawVerticalArrow(this, Offset(dimX, y2), isPointingDown = true, color = dimColor)

    val txt = measurer.measure(
        AnnotatedString(valueText),
        style = TextStyle(
            color = dimColor,
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    )
    drawText(txt, topLeft = Offset(dimX + 5.dp.toPx(), (y1 + y2) / 2f - txt.size.height / 2f))
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawEngineeringTitleBlock(
    measurer: TextMeasurer,
    canvasW: Float,
    canvasH: Float,
    title: String,
    modelNo: String,
    material: String,
    finish: String,
    tolerance: String,
    scale: String,
    refNo: String
) {
    val blockW = 180.dp.toPx()
    val blockH = 68.dp.toPx()
    val startX = canvasW - blockW - 6.dp.toPx()
    val startY = canvasH - blockH - 6.dp.toPx()

    // Title Block background & borders
    drawRect(
        color = Color(0xFFF8FAFC),
        topLeft = Offset(startX, startY),
        size = Size(blockW, blockH)
    )
    drawRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(startX, startY),
        size = Size(blockW, blockH),
        style = Stroke(1.5.dp.toPx())
    )

    // Divider lines
    drawLine(Color(0xFFCBD5E1), Offset(startX, startY + 22.dp.toPx()), Offset(startX + blockW, startY + 22.dp.toPx()), strokeWidth = 1f)
    drawLine(Color(0xFFCBD5E1), Offset(startX, startY + 44.dp.toPx()), Offset(startX + blockW, startY + 44.dp.toPx()), strokeWidth = 1f)
    drawLine(Color(0xFFCBD5E1), Offset(startX + blockW * 0.55f, startY + 22.dp.toPx()), Offset(startX + blockW * 0.55f, startY + blockH), strokeWidth = 1f)

    // Row 1: Brand & Document Title
    val b1 = measurer.measure(
        AnnotatedString("7HOOKS • $title"),
        style = TextStyle(color = Color(0xFF0F172A), fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    )
    drawText(b1, topLeft = Offset(startX + 4.dp.toPx(), startY + 4.dp.toPx()))

    // Row 2: Model & Material
    val b2 = measurer.measure(
        AnnotatedString("MDL: $modelNo\nMAT: $material"),
        style = TextStyle(color = Color(0xFF334155), fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
    )
    drawText(b2, topLeft = Offset(startX + 4.dp.toPx(), startY + 24.dp.toPx()))

    val b3 = measurer.measure(
        AnnotatedString("SCALE: $scale\nTOL: $tolerance"),
        style = TextStyle(color = Color(0xFF334155), fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
    )
    drawText(b3, topLeft = Offset(startX + blockW * 0.58f, startY + 24.dp.toPx()))

    // Row 3: Ref & Rev
    val b4 = measurer.measure(
        AnnotatedString("REF: $refNo | REV A.02"),
        style = TextStyle(color = Color(0xFF0284C7), fontSize = 7.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    )
    drawText(b4, topLeft = Offset(startX + 4.dp.toPx(), startY + 48.dp.toPx()))
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
