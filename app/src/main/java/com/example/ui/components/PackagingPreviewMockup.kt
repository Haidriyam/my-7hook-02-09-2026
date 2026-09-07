package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PackagingConfiguration

@Composable
fun PackagingPreviewMockup(
    config: PackagingConfiguration,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalW = maxWidth
            val totalH = maxHeight

            // Centered Retail Mockup Container
            val cardW = (totalW * 0.58f).coerceIn(160.dp, 240.dp)
            val cardH = 240.dp

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Engineering drafting grid
                val gridSpacing = 20.dp.toPx()
                val gridColor = Color(0x150284C7)
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

                // Shadow under card
                val cW = cardW.toPx()
                val cH = cardH.toPx()
                val startX = (w - cW) / 2f
                val startY = (h - cH) / 2f + 10.dp.toPx()

                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.12f),
                    topLeft = Offset(startX + 6.dp.toPx(), startY + 6.dp.toPx()),
                    size = Size(cW, cH),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }

            // The actual retail card container
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(cardW)
                    .height(cardH)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Euro Slot Header Tab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Euro slot hole
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(9.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(4.dp))
                        )
                    }

                    // 2. Header Brand Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "7HOOKS",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "COMMERCIAL TACKLE",
                                color = Color(0xFF38BDF8),
                                fontSize = 6.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Logo display or Badge
                        if (config.customLogoUri != null) {
                            AsyncImage(
                                model = Uri.parse(config.customLogoUri),
                                contentDescription = "Distributor Logo",
                                modifier = Modifier
                                    .size(36.dp, 20.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.95f))
                                    .padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(3.dp)
                            ) {
                                Text(
                                    text = config.companyName.take(12),
                                    color = Color(0xFFF97316),
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. Clear Blister Window Cutout with Tackle Mockup inside
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF091322))
                            .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Subtle tackle silhouette / blister sheen
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val pw = size.width
                            val ph = size.height

                            // Blister reflection sheen
                            val sheen = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(pw * 0.35f, 0f)
                                lineTo(pw * 0.15f, ph)
                                lineTo(0f, ph)
                                close()
                            }
                            drawPath(sheen, color = Color.White.copy(alpha = 0.05f))

                            // Lure / Jig silhouette inside window
                            val jigL = pw * 0.65f
                            val jigH = ph * 0.22f
                            val jx = pw * 0.5f
                            val jy = ph * 0.5f

                            val jigPath = Path().apply {
                                moveTo(jx - jigL / 2, jy)
                                cubicTo(jx - jigL * 0.2f, jy - jigH / 2, jx + jigL * 0.2f, jy - jigH / 2, jx + jigL / 2, jy)
                                cubicTo(jx + jigL * 0.2f, jy + jigH / 2, jx - jigL * 0.2f, jy + jigH / 2, jx - jigL / 2, jy)
                                close()
                            }
                            drawPath(
                                path = jigPath,
                                brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8)))
                            )
                            drawPath(jigPath, color = Color.White.copy(alpha = 0.6f), style = Stroke(width = 1f))
                        }

                        // Product Title Badge inside blister
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = config.productName.take(24),
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "SKU: ${config.modelNumber} • ${config.packagingDimensions}",
                                color = Color(0xFF94A3B8),
                                fontSize = 6.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4. Retail Footer Bar: Barcode & Specs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .background(Color.White, RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Simulated high-density retail barcode
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(18.dp)
                        ) {
                            val barWidths = listOf(2, 1, 3, 1, 2, 4, 1, 2, 1, 3, 2, 1, 4, 2, 1)
                            barWidths.forEach { w ->
                                Box(
                                    modifier = Modifier
                                        .width(w.dp)
                                        .fillMaxHeight()
                                        .background(Color(0xFF0F172A))
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = config.packagingType.take(16).uppercase(),
                                color = Color(0xFF0F172A),
                                fontSize = 6.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "QTY: ${config.targetQuantity}",
                                color = Color(0xFF64748B),
                                fontSize = 5.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Top Left Overlay Badge
            Surface(
                color = Color(0xDD0F172A),
                shape = RoundedCornerShape(bottomEnd = 6.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "RETAIL MOCKUP • ${config.packagingType.uppercase()}",
                    color = Color(0xFF38BDF8),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
