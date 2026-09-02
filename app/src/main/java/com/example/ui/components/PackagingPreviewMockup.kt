package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
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
            .height(260.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                val w = size.width
                val h = size.height

                // Engineering Grid Background
                val gridSpacing = 20.dp.toPx()
                val gridColor = Color(0x180284C7)
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

                val centerX = w * 0.42f
                val centerY = h * 0.52f

                val boxW = 120.dp.toPx()
                val boxH = 150.dp.toPx()
                val depth = 35.dp.toPx()

                // 2.5D Isometric Packaging Box
                // 1. Top Face
                val topPath = Path().apply {
                    moveTo(centerX, centerY - boxH / 2)
                    lineTo(centerX + depth * 0.8f, centerY - boxH / 2 - depth * 0.5f)
                    lineTo(centerX + boxW + depth * 0.8f, centerY - boxH / 2 - depth * 0.5f)
                    lineTo(centerX + boxW, centerY - boxH / 2)
                    close()
                }
                drawPath(topPath, color = Color(0xFF38BDF8))
                drawPath(topPath, color = Color(0xFF0F172A), style = Stroke(width = 1.2.dp.toPx()))

                // 2. Right Side Face
                val sidePath = Path().apply {
                    moveTo(centerX + boxW, centerY - boxH / 2)
                    lineTo(centerX + boxW + depth * 0.8f, centerY - boxH / 2 - depth * 0.5f)
                    lineTo(centerX + boxW + depth * 0.8f, centerY + boxH / 2 - depth * 0.5f)
                    lineTo(centerX + boxW, centerY + boxH / 2)
                    close()
                }
                drawPath(sidePath, color = Color(0xFF0369A1))
                drawPath(sidePath, color = Color(0xFF0F172A), style = Stroke(width = 1.2.dp.toPx()))

                // 3. Front Face
                drawRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(centerX, centerY - boxH / 2),
                    size = Size(boxW, boxH)
                )
                // Front Blue Header Band
                drawRect(
                    color = Color(0xFF0284C7),
                    topLeft = Offset(centerX, centerY - boxH / 2),
                    size = Size(boxW, 35.dp.toPx())
                )

                // Euro Slot Hang Tab
                val tabPath = Path().apply {
                    moveTo(centerX + boxW * 0.35f, centerY - boxH / 2)
                    lineTo(centerX + boxW * 0.35f, centerY - boxH / 2 - 18.dp.toPx())
                    lineTo(centerX + boxW * 0.65f, centerY - boxH / 2 - 18.dp.toPx())
                    lineTo(centerX + boxW * 0.65f, centerY - boxH / 2)
                    close()
                }
                drawPath(tabPath, color = Color(0xFFCBD5E1))
                drawPath(tabPath, color = Color(0xFF0F172A), style = Stroke(width = 1.dp.toPx()))
                // Slot Hole
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(centerX + boxW * 0.42f, centerY - boxH / 2 - 13.dp.toPx()),
                    size = Size(boxW * 0.16f, 6.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // Window / Blister cut-out preview
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(centerX + 10.dp.toPx(), centerY - boxH / 2 + 75.dp.toPx()),
                    size = Size(boxW - 20.dp.toPx(), 55.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF38BDF8),
                    topLeft = Offset(centerX + 10.dp.toPx(), centerY - boxH / 2 + 75.dp.toPx()),
                    size = Size(boxW - 20.dp.toPx(), 55.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Outline front
                drawRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(centerX, centerY - boxH / 2),
                    size = Size(boxW, boxH),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // Overlay Branding Text & Uploaded Company Logo directly on the 2.5D front face
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 135.dp, top = 65.dp)
            ) {
                Column(modifier = Modifier.width(105.dp)) {
                    Text(
                        text = "7HOOKS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "PRECISION TACKLE",
                        color = Color(0xFFBAE6FD),
                        fontSize = 6.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (config.customLogoUri != null) {
                        AsyncImage(
                            model = Uri.parse(config.customLogoUri),
                            contentDescription = "Uploaded Custom Company Logo",
                            modifier = Modifier
                                .height(22.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.9f)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = config.companyName.take(18),
                            color = Color(0xFFF97316),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Text(
                        text = config.productName.take(20),
                        color = Color.White,
                        fontSize = 7.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "MODEL: ${config.modelNumber}",
                        color = Color(0xFF94A3B8),
                        fontSize = 6.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Top Badge
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(bottomEnd = 6.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "2.5D PERSPECTIVE MOCKUP • ${config.packagingType.uppercase()}",
                    color = Color(0xFF38BDF8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Dimension Label Bottom Right
            Surface(
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(topStart = 6.dp),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(
                    text = "DIM: ${config.packagingDimensions}",
                    color = Color(0xFF0F172A),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
