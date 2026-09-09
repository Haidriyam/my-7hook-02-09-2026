package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.geometry.JigGeometryEngine
import com.example.data.geometry.JigGeometryEngine.JigSilhouetteType
import com.example.data.model.JigShapeCategory
import com.example.data.model.JigShapeRepository
import com.example.data.model.JigShapeTemplate
import com.example.ui.components.AppHeader

/**
 * 7Hooks Jig Shape Library Screen (Step 1 of Configurator).
 * Replaces finished commercial products with clean, technical base shape templates.
 * Selecting a shape establishes the visual foundation for progressive configuration.
 */
@Composable
fun JigCatalogScreen(
    onSelectShape: (JigShapeTemplate) -> Unit,
    onNavigateBack: () -> Unit,
    onSelectJig: ((com.example.data.model.JigProduct) -> Unit)? = null
) {
    val allShapes = remember { JigShapeRepository.shapes }
    var selectedCategory by remember { mutableStateOf<JigShapeCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredShapes = remember(selectedCategory, searchQuery) {
        allShapes.filter { shape ->
            val matchesCategory = selectedCategory == null || shape.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    shape.shapeName.contains(searchQuery, ignoreCase = true) ||
                    shape.shortDescription.contains(searchQuery, ignoreCase = true) ||
                    shape.bodyProfile.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Choose Jig Shape",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = Modifier.testTag("jig_shape_library_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // High-End Header Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Architecture,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "SHAPE-FIRST CAD WORKFLOW",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Select a Hydrodynamic Silhouette",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose a foundational geometry below. Your selected silhouette will remain visible and evolve step-by-step as you configure dimensions, colors, finishes, and rigging.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Search Bar & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shape_search_input"),
                        placeholder = { Text("Search 50+ shapes (e.g. Round, Arkie, Shad, Blade)...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF0F172A),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    // Horizontal Category Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All (${allShapes.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0F172A),
                                selectedLabelColor = Color.White
                            )
                        )

                        JigShapeCategory.values().forEach { cat ->
                            val count = allShapes.count { it.category == cat }
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                                label = { Text("${cat.displayName} ($count)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0F172A),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Shape Template Cards
            items(filteredShapes) { shape ->
                JigShapeCard(
                    shape = shape,
                    onClick = { onSelectShape(shape) }
                )
            }
        }
    }
}

/**
 * Individual Jig Shape Card with clean CAD silhouette preview.
 */
@Composable
private fun JigShapeCard(
    shape: JigShapeTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .testTag("shape_card_${shape.shapeId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // SILHOUETTE CANVAS PREVIEW
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background subtle technical grid lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 20f
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = Color(0x180F172A),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += step
                    }

                    // Floor contact shadow
                    val cx = size.width / 2f
                    val cy = size.height / 2f + 25f
                    drawOval(
                        color = Color(0x220F172A),
                        topLeft = Offset(cx - size.width * 0.35f, cy),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.70f, 16f)
                    )

                    // Draw the pure technical silhouette
                    val bodyL = size.width * 0.75f
                    val bodyW = (bodyL / shape.aspectRatio).coerceIn(24f, size.height * 0.75f)
                    val halfL = bodyL / 2f
                    val halfW = bodyW / 2f

                    val path = buildSimpleSilhouettePath(shape.silhouetteType, cx, size.height / 2f, halfL, halfW)

                    // Technical brushed steel fill
                    val shapeBrush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF334155), Color(0xFF1E293B)),
                        startY = size.height / 2f - halfW,
                        endY = size.height / 2f + halfW
                    )
                    drawPath(path = path, brush = shapeBrush)

                    // Precision white contour edge
                    drawPath(
                        path = path,
                        color = Color(0xFF0284C7),
                        style = Stroke(width = 1.8f, cap = StrokeCap.Round)
                    )

                    // Centerline
                    drawLine(
                        color = Color(0x4438BDF8),
                        start = Offset(cx - halfL * 0.9f, size.height / 2f),
                        end = Offset(cx + halfL * 0.9f, size.height / 2f),
                        strokeWidth = 1f
                    )
                }

                // Category & Aspect Ratio Pills
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xEE0284C7)
                    ) {
                        Text(
                            text = shape.category.displayName.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xDD0F172A)
                    ) {
                        Text(
                            text = "1:${String.format("%.1f", shape.aspectRatio)}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            color = Color(0xFF38BDF8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // TEXT SPECIFICATIONS & SELECT ACTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = shape.shapeName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE0F2FE)
                    ) {
                        Text(
                            text = "${shape.defaultWeightGrams.toInt()}g / ${shape.defaultLengthMm.toInt()}mm",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color(0xFF0369A1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = shape.shortDescription,
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )

                Text(
                    text = shape.bodyProfile,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weight: ${shape.minWeightGrams.toInt()}g - ${shape.maxWeightGrams.toInt()}g",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Configure Shape",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun buildSimpleSilhouettePath(
    type: JigSilhouetteType,
    cx: Float,
    cy: Float,
    halfL: Float,
    halfW: Float
): Path {
    return JigGeometryEngine.buildFrontSilhouettePath(type, cx, cy, halfL, halfW).asComposePath()
}
