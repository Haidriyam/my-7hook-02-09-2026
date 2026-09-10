package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextOverflow
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
 * Presents shapes in a 2x2 grid format with the best/unique shapes visible on top,
 * followed by distinct sections as you scroll down.
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

    // Top 4 Signature / Best Shapes
    val topSignatureShapes = remember(allShapes) {
        val signatureIds = listOf("flutter", "knife", "asymmetric_keel", "diamond")
        val topFound = signatureIds.mapNotNull { id -> allShapes.find { it.shapeId == id } }
        if (topFound.size == 4) topFound else allShapes.filter { it.category == JigShapeCategory.SIGNATURE }.take(4)
    }

    val pelagicShapes = remember(allShapes) {
        allShapes.filter { it.category == JigShapeCategory.FISHING_BODY && !topSignatureShapes.contains(it) }
    }

    val specialtyHeadShapes = remember(allShapes) {
        allShapes.filter { it.category == JigShapeCategory.HEAD_JIG_STYLES && !topSignatureShapes.contains(it) }
    }

    val cutSilhouetteShapes = remember(allShapes) {
        allShapes.filter { it.category == JigShapeCategory.SPECIAL_SILHOUETTES && !topSignatureShapes.contains(it) }
    }

    val basicGeometricShapes = remember(allShapes) {
        allShapes.filter { it.category == JigShapeCategory.BASIC_GEOMETRIC && !topSignatureShapes.contains(it) }
    }

    val searchFilteredShapes = remember(selectedCategory, searchQuery, allShapes) {
        if (searchQuery.isBlank() && selectedCategory == null) {
            emptyList()
        } else {
            allShapes.filter { shape ->
                val matchesCat = selectedCategory == null || shape.category == selectedCategory
                val matchesQ = searchQuery.isBlank() ||
                        shape.shapeName.contains(searchQuery, ignoreCase = true) ||
                        shape.shortDescription.contains(searchQuery, ignoreCase = true) ||
                        shape.bodyProfile.contains(searchQuery, ignoreCase = true)
                matchesCat && matchesQ
            }
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            // COMPACT TOP HEADER & SEARCH
            item(span = { GridItemSpan(2) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shape_search_input"),
                        placeholder = { Text("Search shapes (Flutter, Knife, Arkie, Shad)...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
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

                    // Category Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null && searchQuery.isBlank(),
                            onClick = {
                                selectedCategory = null
                                searchQuery = ""
                            },
                            label = { Text("All Sections (${allShapes.size})", fontSize = 11.sp) },
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

            // IF FILTER/SEARCH ACTIVE: SHOW FILTERED RESULTS
            if (searchQuery.isNotBlank() || selectedCategory != null) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "Filtered Shapes (${searchFilteredShapes.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                items(searchFilteredShapes) { shape ->
                    JigShapeGridCard(
                        shape = shape,
                        isTopSignature = topSignatureShapes.contains(shape),
                        onClick = { onSelectShape(shape) }
                    )
                }
            } else {
                // =============================================================
                // SECTION 1: BEST & MOST UNIQUE SHAPES (FIRST VIEW: 2 COLUMNS × 2 ROWS = 4)
                // =============================================================
                item(span = { GridItemSpan(2) }) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "BEST & MOST UNIQUE SHAPES",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "4 SIGNATURES",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 4 TOP SIGNATURE SHAPES IN 2x2 GRID (First View)
                items(topSignatureShapes) { shape ->
                    JigShapeGridCard(
                        shape = shape,
                        isTopSignature = true,
                        onClick = { onSelectShape(shape) }
                    )
                }

                // =============================================================
                // SECTION 2: PELAGIC & BAITFISH PROFILES (SCROLL DOWN FOR MORE)
                // =============================================================
                if (pelagicShapes.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        SectionHeader("🎣 PELAGIC & BAITFISH PROFILES", "${pelagicShapes.size} Shapes")
                    }
                    items(pelagicShapes) { shape ->
                        JigShapeGridCard(shape = shape, isTopSignature = false, onClick = { onSelectShape(shape) })
                    }
                }

                // =============================================================
                // SECTION 3: SPECIALTY HEAD & STRUCTURE JIGS
                // =============================================================
                if (specialtyHeadShapes.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        SectionHeader("⚙️ SPECIALTY HEAD & STRUCTURE JIGS", "${specialtyHeadShapes.size} Shapes")
                    }
                    items(specialtyHeadShapes) { shape ->
                        JigShapeGridCard(shape = shape, isTopSignature = false, onClick = { onSelectShape(shape) })
                    }
                }

                // =============================================================
                // SECTION 4: ASYMMETRIC & CUT SILHOUETTES
                // =============================================================
                if (cutSilhouetteShapes.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        SectionHeader("✨ SPECIAL ASYMMETRIC & CUT PRISMS", "${cutSilhouetteShapes.size} Shapes")
                    }
                    items(cutSilhouetteShapes) { shape ->
                        JigShapeGridCard(shape = shape, isTopSignature = false, onClick = { onSelectShape(shape) })
                    }
                }

                // =============================================================
                // SECTION 5: BASIC GEOMETRIC BALLAST
                // =============================================================
                if (basicGeometricShapes.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        SectionHeader("📐 BASIC GEOMETRIC BALLAST", "${basicGeometricShapes.size} Shapes")
                    }
                    items(basicGeometricShapes) { shape ->
                        JigShapeGridCard(shape = shape, isTopSignature = false, onClick = { onSelectShape(shape) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, badge: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF0284C7)
        )
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFE0F2FE)
        ) {
            Text(
                text = badge,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF0369A1)
            )
        }
    }
}

/**
 * Compact 2-column Grid Card displaying the technical silhouette and specs.
 */
@Composable
private fun JigShapeGridCard(
    shape: JigShapeTemplate,
    isTopSignature: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isTopSignature) 1.5.dp else 1.dp,
                color = if (isTopSignature) Color(0xFF38BDF8) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("shape_card_${shape.shapeId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTopSignature) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // SILHOUETTE CANVAS PREVIEW
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = if (isTopSignature) {
                                listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE))
                            } else {
                                listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
                            }
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    // Subtle floor contact shadow
                    drawOval(
                        color = Color(0x200F172A),
                        topLeft = Offset(cx - size.width * 0.38f, cy + 20f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.76f, 10f)
                    )

                    val bodyL = size.width * 0.82f
                    val bodyW = (bodyL / shape.aspectRatio).coerceIn(18f, size.height * 0.75f)
                    val halfL = bodyL / 2f
                    val halfW = bodyW / 2f

                    val path = buildSimpleSilhouettePath(shape.silhouetteType, cx, cy, halfL, halfW)

                    // Brushed alloy gradient fill
                    val shapeBrush = Brush.verticalGradient(
                        colors = if (isTopSignature) {
                            listOf(Color(0xFF0369A1), Color(0xFF0F172A))
                        } else {
                            listOf(Color(0xFF475569), Color(0xFF1E293B))
                        },
                        startY = cy - halfW,
                        endY = cy + halfW
                    )
                    drawPath(path = path, brush = shapeBrush)

                    // Precision contour edge
                    drawPath(
                        path = path,
                        color = if (isTopSignature) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                        style = Stroke(width = 1.4f, cap = StrokeCap.Round)
                    )
                }

                // Aspect ratio / Signature badge
                if (isTopSignature) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xEE0F172A),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "TOP",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color = Color(0xFF38BDF8),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // SPECIFICATIONS & TITLE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = shape.shapeName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${shape.defaultWeightGrams.toInt()}g • ${shape.defaultLengthMm.toInt()}mm",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF0284C7)
                    )
                    Text(
                        text = "1:${String.format("%.1f", shape.aspectRatio)}",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B)
                    )
                }

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTopSignature) Color(0xFF0284C7) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth().height(30.dp)
                ) {
                    Text(
                        text = "Select Shape",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
