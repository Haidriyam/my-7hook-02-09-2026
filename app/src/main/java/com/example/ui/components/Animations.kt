package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductConfiguration
import kotlin.math.sin

@Composable
fun JigFishingAnimation(
    config: ProductConfiguration,
    modifier: Modifier = Modifier
) {
    // 2.5 second looping fishing action
    val infiniteTransition = rememberInfiniteTransition(label = "jig_fishing_anim")
    
    val cycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cycle_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2442)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val w = size.width
                val h = size.height

                // Deep sea gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F3662), Color(0xFF08182D)),
                        startY = 0f,
                        endY = h
                    )
                )

                // Sun rays underwater
                for (i in 0..4) {
                    val rayPath = Path().apply {
                        moveTo(w * (0.2f + i * 0.18f), 0f)
                        lineTo(w * (0.35f + i * 0.18f), h)
                        lineTo(w * (0.28f + i * 0.18f), h)
                        lineTo(w * (0.15f + i * 0.18f), 0f)
                        close()
                    }
                    drawPath(rayPath, color = Color.White.copy(alpha = 0.04f))
                }

                // Rising air bubbles
                for (b in 0..5) {
                    val bubbleProgress = (cycleProgress + b * 0.16f) % 1f
                    val bx = w * (0.2f + b * 0.13f) + sin((bubbleProgress * 6.28f).toDouble()).toFloat() * 10f
                    val by = h * (1f - bubbleProgress)
                    drawCircle(
                        color = Color(0x6638BDF8),
                        radius = (2.5f + (b % 3)).dp.toPx(),
                        center = Offset(bx, by),
                        style = Stroke(width = 1f)
                    )
                }

                // Jig vertical jerk & flutter motion
                // Phase 0..0.4: Jerk UP, Phase 0.4..0.8: Flutter DOWN, Phase 0.8..1.0: Strike
                val jigX = w * 0.48f
                val baseJigY = h * 0.55f
                val jigYOffset = when {
                    cycleProgress < 0.35f -> {
                        // Jerk upward sharply
                        val t = cycleProgress / 0.35f
                        -35.dp.toPx() * sin((t * Math.PI / 2).toDouble()).toFloat()
                    }
                    cycleProgress < 0.75f -> {
                        // Flutter downward with wobble
                        val t = (cycleProgress - 0.35f) / 0.4f
                        -35.dp.toPx() * (1f - t) + sin((t * Math.PI * 4).toDouble()).toFloat() * 6.dp.toPx()
                    }
                    else -> 0f
                }
                val currentJigY = baseJigY + jigYOffset
                val jigAngle = sin((cycleProgress * Math.PI * 4).toDouble()).toFloat() * 18f

                // Fishing Fluorocarbon Line from top
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(jigX, 0f),
                    end = Offset(jigX, currentJigY - 20.dp.toPx()),
                    strokeWidth = 1.2.dp.toPx()
                )

                // Draw Configured Jig in action
                val jigL = 36.dp.toPx()
                val jigW = 10.dp.toPx()

                val jigPath = Path().apply {
                    moveTo(jigX, currentJigY - jigL / 2)
                    cubicTo(
                        jigX - jigW / 2, currentJigY - jigL / 4,
                        jigX - jigW / 2, currentJigY + jigL / 4,
                        jigX, currentJigY + jigL / 2
                    )
                    cubicTo(
                        jigX + jigW / 2, currentJigY + jigL / 4,
                        jigX + jigW / 2, currentJigY - jigL / 4,
                        jigX, currentJigY - jigL / 2
                    )
                    close()
                }

                drawPath(
                    path = jigPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(config.baseColorHex), Color(config.accentColorHex)),
                        startX = jigX - jigW,
                        endX = jigX + jigW
                    )
                )
                drawPath(jigPath, color = Color.White.copy(alpha = 0.8f), style = Stroke(width = 1.2f))

                // Flash glow around jig during jerk
                if (cycleProgress in 0.1f..0.35f) {
                    drawCircle(
                        color = Color(0x5538BDF8),
                        radius = 20.dp.toPx(),
                        center = Offset(jigX, currentJigY)
                    )
                }

                // Split Ring & Assist Hook on Jig
                val threadColor = config.threadColorHex?.let { Color(it) } 
                    ?: when (config.threadColor.lowercase()) {
                        "red" -> Color(0xFFDC2626)
                        "orange" -> Color(0xFFEA580C)
                        "yellow" -> Color(0xFFEAB308)
                        "green" -> Color(0xFF16A34A)
                        "blue" -> Color(0xFF0284C7)
                        "black" -> Color(0xFF1E293B)
                        "white" -> Color(0xFFF8FAFC)
                        "pink" -> Color(0xFFEC4899)
                        "purple" -> Color(0xFF9333EA)
                        "gold" -> Color(0xFFE2B024)
                        "silver" -> Color(0xFFCBD5E1)
                        else -> null
                    }

                // Split Ring (SUS304 Stainless)
                drawCircle(
                    color = Color(0xFFCBD5E1),
                    radius = 2.5.dp.toPx(),
                    center = Offset(jigX, currentJigY - jigL / 2 - 2.dp.toPx()),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Assist Cord (Braided PE)
                val hookPointX = jigX - 11.dp.toPx()
                val hookPointY = currentJigY - jigL / 2 + 15.dp.toPx()
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(jigX, currentJigY - jigL / 2),
                    end = Offset(hookPointX, hookPointY),
                    strokeWidth = 1.8.dp.toPx()
                )

                // Thread whip wrap around assist hook shank (if thread is configured)
                if (threadColor != null && config.threadColor != "None") {
                    drawLine(
                        color = threadColor,
                        start = Offset(jigX - 4.dp.toPx(), currentJigY - jigL / 2 + 6.dp.toPx()),
                        end = Offset(jigX - 9.dp.toPx(), currentJigY - jigL / 2 + 13.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Assist Hook (Forged High-Carbon Steel)
                drawCircle(
                    color = Color(0xFF94A3B8),
                    radius = 3.5.dp.toPx(),
                    center = Offset(hookPointX, hookPointY),
                    style = Stroke(width = 1.4f)
                )

                // Water current & hydrodynamic vortex trails behind flutter
                for (v in 0..3) {
                    val vortexProgress = (cycleProgress * 2f + v * 0.25f) % 1f
                    val vy = currentJigY + 18.dp.toPx() + vortexProgress * 24.dp.toPx()
                    val vxOffset = if (v % 2 == 0) -12.dp.toPx() else 12.dp.toPx()
                    drawCircle(
                        color = Color(0x2238BDF8),
                        radius = (3f + vortexProgress * 6f).dp.toPx(),
                        center = Offset(jigX + vxOffset * (1f - vortexProgress), vy),
                        style = Stroke(width = 1f)
                    )
                }

                // Hydrodynamic drift lines
                for (i in 0..2) {
                    val streamX = w * (0.15f + i * 0.35f)
                    val streamProgress = (cycleProgress + i * 0.33f) % 1f
                    drawLine(
                        color = Color(0x1838BDF8),
                        start = Offset(streamX, streamProgress * h),
                        end = Offset(streamX + 15.dp.toPx(), (streamProgress * h) + 25.dp.toPx()),
                        strokeWidth = 1f
                    )
                }
            }

            // Overlay Badge
            Surface(
                color = Color(0xDD0F172A),
                shape = RoundedCornerShape(bottomEnd = 8.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "PRODUCT IN-USE DEMONSTRATION • Hydrodynamic Balance & Flutter Sink",
                    color = Color(0xFF38BDF8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun RodBreakageAnimation(
    config: ProductConfiguration,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var interactiveLoadPercent by remember { mutableFloatStateOf(0f) }

    val autoAnimProgress = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                autoAnimProgress.snapTo(0f)
                autoAnimProgress.animateTo(
                    targetValue = 1.2f, // Goes up to 120% load to show failure threshold
                    animationSpec = tween(3200, easing = LinearEasing)
                )
                kotlinx.coroutines.delay(1200)
            }
        }
    }

    val currentLoadFactor = if (isPlaying) autoAnimProgress.value else (interactiveLoadPercent / 100f)
    val isBroken = currentLoadFactor >= 1.05f
    val currentKg = (config.maximumLoadKg * currentLoadFactor).coerceAtLeast(0f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        // Animation Header & Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ROD STRENGTH & LOAD VISUALIZATION",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = if (isBroken) "FAILURE THRESHOLD EXCEEDED (STRUCTURAL SNAP)"
                           else "Applied Load: ${String.format(java.util.Locale.US, "%.1f", currentKg)} kg / Max: ${String.format(java.util.Locale.US, "%.1f", config.maximumLoadKg)} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isBroken) Color(0xFFEF4444) else Color(0xFFCBD5E1)
                )
            }

            FilledTonalButton(
                onClick = { isPlaying = !isPlaying },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isPlaying) Color(0xFF0284C7) else Color(0xFF334155),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = if (isPlaying) "Auto Loop" else "Manual", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Visual Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(Color(0xFF080D1A), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                val w = size.width
                val h = size.height

                // Blueprint background grid
                val gridSpacing = 20.dp.toPx()
                val gridColor = Color(0x1538BDF8)
                var gx = 0f
                while (gx <= w) {
                    drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                    gx += gridSpacing
                }
                var gy = 0f
                while (gy <= h) {
                    drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                    gy += gridSpacing
                }

                // Rod Base Anchor (left handle fixed)
                val buttX = 20.dp.toPx()
                val buttY = h * 0.28f
                val rodLengthPx = w - 60.dp.toPx()

                // Calculate deflection curve
                // Deflection angle and tip drop increases quadratically with load factor
                val deflectionFactor = currentLoadFactor.coerceIn(0f, 1.2f)
                val maxTipDrop = h * 0.58f * deflectionFactor
                val maxTipPullBack = rodLengthPx * 0.22f * (deflectionFactor * deflectionFactor)

                val tipX = buttX + rodLengthPx - maxTipPullBack
                val tipY = buttY + maxTipDrop

                val handleLen = rodLengthPx * 0.2f

                // Draw Handle (Rigid straight grip)
                drawRoundRect(
                    color = Color(0xFF475569),
                    topLeft = Offset(buttX, buttY - 6.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(handleLen, 12.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )

                if (!isBroken) {
                    // Intact Deflected Rod Curve (Parabolic blank curve)
                    val rodPath = Path().apply {
                        moveTo(buttX + handleLen, buttY)
                        // Dynamic cubic bezier simulating carbon blank taper flex
                        cubicTo(
                            buttX + handleLen + (tipX - (buttX + handleLen)) * 0.45f, buttY,
                            buttX + handleLen + (tipX - (buttX + handleLen)) * 0.85f, buttY + maxTipDrop * 0.6f,
                            tipX, tipY
                        )
                    }

                    // Draw blank with load stress color gradient (Green -> Orange -> Red)
                    val stressColor = when {
                        currentLoadFactor < 0.6f -> Color(0xFF38BDF8)
                        currentLoadFactor < 0.95f -> Color(0xFFF97316)
                        else -> Color(0xFFEF4444)
                    }

                    drawPath(
                        path = rodPath,
                        color = stressColor,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Line hanging down with load/fish weight
                    val weightY = (tipY + 45.dp.toPx()).coerceAtMost(h - 10.dp.toPx())
                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = Offset(tipX, tipY),
                        end = Offset(tipX, weightY),
                        strokeWidth = 1.2.dp.toPx()
                    )

                    // Hanging Catch Fish Target
                    val fishX = tipX
                    val fishY = weightY + 6.dp.toPx()
                    val fishPath = Path().apply {
                        moveTo(fishX - 10.dp.toPx(), fishY)
                        lineTo(fishX + 10.dp.toPx(), fishY - 6.dp.toPx())
                        lineTo(fishX + 16.dp.toPx(), fishY - 10.dp.toPx())
                        lineTo(fishX + 13.dp.toPx(), fishY)
                        lineTo(fishX + 16.dp.toPx(), fishY + 10.dp.toPx())
                        lineTo(fishX + 10.dp.toPx(), fishY + 6.dp.toPx())
                        close()
                    }
                    drawPath(fishPath, color = Color(0xFF0284C7))

                    // Tip Guide
                    drawCircle(Color(0xFFF97316), radius = 3.dp.toPx(), center = Offset(tipX, tipY))
                } else {
                    // Rod is snapped at high stress failure point (~65% along blank)
                    val breakRatio = 0.62f
                    val breakX = buttX + handleLen + (tipX - (buttX + handleLen)) * breakRatio
                    val breakY = buttY + maxTipDrop * 0.45f

                    // Base intact segment
                    val baseSegment = Path().apply {
                        moveTo(buttX + handleLen, buttY)
                        cubicTo(
                            buttX + handleLen + (breakX - (buttX + handleLen)) * 0.5f, buttY,
                            breakX - 15.dp.toPx(), breakY - 5.dp.toPx(),
                            breakX, breakY
                        )
                    }
                    drawPath(baseSegment, color = Color(0xFFEF4444), style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))

                    // Broken snapped tip segment falling downwards
                    val brokenTip = Path().apply {
                        moveTo(breakX + 4.dp.toPx(), breakY + 6.dp.toPx())
                        lineTo(breakX + 25.dp.toPx(), breakY + 38.dp.toPx())
                    }
                    drawPath(brokenTip, color = Color(0xFFEF4444), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

                    // Structural fracture line indicator (clean finite element stress failure point)
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(breakX - 4.dp.toPx(), breakY - 4.dp.toPx()),
                        end = Offset(breakX + 4.dp.toPx(), breakY + 4.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        // Manual Slider if not playing auto
        if (!isPlaying) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Simulated Load: ", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Slider(
                    value = interactiveLoadPercent,
                    onValueChange = { interactiveLoadPercent = it },
                    valueRange = 0f..120f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = if (interactiveLoadPercent > 100f) Color(0xFFEF4444) else Color(0xFF38BDF8),
                        activeTrackColor = if (interactiveLoadPercent > 100f) Color(0xFFEF4444) else Color(0xFF38BDF8)
                    )
                )
                Text("${interactiveLoadPercent.toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Mandatory Disclaimer Notice (Section 13)
        Surface(
            color = Color(0x22F59E0B),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Illustrative simulation — actual rod performance requires physical testing.",
                color = Color(0xFFFCD34D),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}
