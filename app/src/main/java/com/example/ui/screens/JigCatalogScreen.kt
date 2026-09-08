package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.JigProduct
import com.example.data.model.ProductCatalog
import com.example.ui.components.*

@Composable
fun JigCatalogScreen(
    onSelectJig: (JigProduct) -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentYear = remember { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }
    val featuredJigs = remember { ProductCatalog.newJigs.take(2) }
    val otherJigs = remember { ProductCatalog.jigs.filter { it !in featuredJigs } }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Jigs",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        modifier = Modifier.testTag("jig_catalog_screen")
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // STEP 1 OF 3 Guided Progress Indicator
            item(span = { GridItemSpan(maxLineSpan) }) {
                TactileStepIndicator(
                    currentStep = 1,
                    totalSteps = 3,
                    stepTitles = listOf("Choose Jig", "Technical Specs", "Technical Drawing"),
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
            }

            // Subtitle Header Card
            item(span = { GridItemSpan(maxLineSpan) }) {
                TactileCard(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Select a Jig to Configure",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Choose from tournament classics or heavy sea models to customize alloy weight, length, hydrodynamic hydrofoil finish, and generate engineering technical drawings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // SECTION 1: FEATURED RECOMMENDED JIGS (Only 2 displayed, dynamic year)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFFEA580C),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Our Recommended",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "$currentYear Innovation Series (Featured First)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(featuredJigs, key = { it.id }) { jig ->
                JigProductCard(
                    jig = jig,
                    isNew = true,
                    onClick = { onSelectJig(jig) }
                )
            }

            // SECTION 2: OTHER & TOURNAMENT JIGS
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF0284C7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "PRODUCT CATALOG",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Classic Tournament Pelagic Series",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(otherJigs, key = { it.id }) { jig ->
                JigProductCard(
                    jig = jig,
                    isNew = false,
                    onClick = { onSelectJig(jig) }
                )
            }
        }
    }
}

@Composable
private fun JigProductCard(
    jig: JigProduct,
    isNew: Boolean,
    onClick: () -> Unit
) {
    TactileCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("jig_card_${jig.id}"),
        shadowElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isNew) Color(0xFFF97316).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Controlled Image Container (Restricted frame ~140dp, ContentScale.Fit)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = jig.imageUrl,
                    contentDescription = jig.name,
                    contentScale = ContentScale.Fit,
                    error = painterResource(id = R.drawable.ic_jig_icon),
                    fallback = painterResource(id = R.drawable.ic_jig_icon),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )

                // Realistic Material Texture Swatch in Corner
                Box(
                    modifier = Modifier
                        .size(28.dp, 20.dp)
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 4.dp, end = 4.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .shadow(2.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRealisticFinishTexture(
                            baseColor = Color(jig.baseColorHex),
                            accentColor = Color(jig.accentColorHex),
                            pattern = jig.patternType
                        )
                    }
                }

                if (isNew) {
                    Surface(
                        color = Color(0xFFEA580C),
                        shape = RoundedCornerShape(bottomEnd = 6.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "NEW 2026",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = jig.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = "${jig.category} • ${jig.modelNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = jig.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 2,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            TactileButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                variant = TactileButtonVariant.PRIMARY,
                icon = Icons.Default.Tune,
                text = "Configure Jig",
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
}
