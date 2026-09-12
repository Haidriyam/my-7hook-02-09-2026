package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.PackagingConfiguration

/**
 * Visual display perspectives for the packaging mockup.
 */
enum class PackagingPerspective(val label: String, val subtitle: String) {
    THREE_D("3D Perspective", "Isometric 3D depth & bevels"),
    FRONT_STUDIO("Front Studio", "Catalogue presentation"),
    DIELINE_FLAT("Dieline Blueprint", "Unfolded die-cut engineering"),
    RETAIL_PEGBOARD("Pegboard Hook", "Commercial retail rack")
}

/**
 * Finish and material themes for the packaging presentation.
 */
enum class MockupFinishTheme(
    val displayName: String,
    val primaryBg: Color,
    val cardSurface: Color,
    val accentFoil: Color,
    val textColor: Color,
    val subtitleColor: Color,
    val isDark: Boolean
) {
    MIDNIGHT_CYAN(
        "Midnight & Cyan",
        Color(0xFF090D16),
        Color(0xFF0F172A),
        Color(0xFF38BDF8),
        Color(0xFFF8FAFC),
        Color(0xFF94A3B8),
        true
    ),
    ECO_KRAFT(
        "Eco Kraft Board",
        Color(0xFFB48A5C),
        Color(0xFFC79E70),
        Color(0xFF2B180A),
        Color(0xFF1E1308),
        Color(0xFF4A3525),
        false
    ),
    OBSIDIAN_GOLD(
        "Obsidian & Gold",
        Color(0xFF121214),
        Color(0xFF1C1C20),
        Color(0xFFF59E0B),
        Color(0xFFFFFBEB),
        Color(0xFFD97706),
        true
    ),
    CLEAN_WHITE(
        "Pure White Gloss",
        Color(0xFFE2E8F0),
        Color(0xFFFFFFFF),
        Color(0xFF0284C7),
        Color(0xFF0F172A),
        Color(0xFF64748B),
        false
    )
}

/**
 * High-end, dynamically realistic packaging mockup preview.
 * Reacts automatically to packaging type, perspective angles, materials, typography, and dimension details.
 */
@Composable
fun PackagingPreviewMockup(
    config: PackagingConfiguration,
    perspective: PackagingPerspective = PackagingPerspective.THREE_D,
    theme: MockupFinishTheme = MockupFinishTheme.MIDNIGHT_CYAN,
    productInsertImageUrl: String = "http://7hooks.com/wp-content/uploads/2026/05/WhatsApp-Image-2026-05-08-at-3.13.03-PM.jpeg",
    productInsertName: String = "Orange-Black Jig",
    showDimensions: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Parse dimensions cleanly
    val dims = remember(config.packagingDimensions) {
        parsePackagingDimensions(config.packagingDimensions)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .testTag("packaging_preview_mockup"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111E)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E293B).copy(alpha = 0.6f),
                            Color(0xFF070B12)
                        ),
                        radius = 800f
                    )
                )
        ) {
            val totalW = maxWidth
            val totalH = maxHeight

            // 1. Perspective Background Canvas (Engineering Grid, Pegboard, or Studio Floor)
            Canvas(modifier = Modifier.fillMaxSize()) {
                when (perspective) {
                    PackagingPerspective.DIELINE_FLAT -> drawDielineBlueprintGrid(size)
                    PackagingPerspective.RETAIL_PEGBOARD -> drawRetailPegboardBackground(size)
                    PackagingPerspective.FRONT_STUDIO -> drawStudioPhotographyLighting(size)
                    PackagingPerspective.THREE_D -> drawIsometricSurfaceGrid(size)
                }
            }

            // 2. Package Mockup Container according to Packaging Type & Perspective
            val packageTypeNorm = config.packagingType.lowercase()

            when {
                packageTypeNorm.contains("dieline") || perspective == PackagingPerspective.DIELINE_FLAT -> {
                    DielineBlueprintView(
                        config = config,
                        dims = dims,
                        theme = theme,
                        showDimensions = showDimensions,
                        modifier = Modifier.fillMaxSize().padding(12.dp)
                    )
                }
                packageTypeNorm.contains("polybag") || packageTypeNorm.contains("header") -> {
                    HeaderCardPolybagMockup(
                        config = config,
                        dims = dims,
                        theme = theme,
                        perspective = perspective,
                        productImageUrl = productInsertImageUrl,
                        productName = productInsertName,
                        showDimensions = showDimensions,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                packageTypeNorm.contains("rigid") || packageTypeNorm.contains("gift") -> {
                    RigidGiftBoxMockup(
                        config = config,
                        dims = dims,
                        theme = theme,
                        perspective = perspective,
                        productImageUrl = productInsertImageUrl,
                        productName = productInsertName,
                        showDimensions = showDimensions,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                packageTypeNorm.contains("bulk") || packageTypeNorm.contains("oem") || packageTypeNorm.contains("carton") -> {
                    BulkOemMasterPackMockup(
                        config = config,
                        dims = dims,
                        theme = theme,
                        perspective = perspective,
                        productImageUrl = productInsertImageUrl,
                        showDimensions = showDimensions,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                packageTypeNorm.contains("clamshell") || packageTypeNorm.contains("blister") -> {
                    ClamshellBlisterMockup(
                        config = config,
                        dims = dims,
                        theme = theme,
                        perspective = perspective,
                        productImageUrl = productInsertImageUrl,
                        productName = productInsertName,
                        showDimensions = showDimensions,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    // Default to Retail Hanging Box
                    RetailHangingBoxMockup(
                        config = config,
                        dims = dims,
                        theme = theme,
                        perspective = perspective,
                        productImageUrl = productInsertImageUrl,
                        productName = productInsertName,
                        showDimensions = showDimensions,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // 3. Top-Left Overlay Badges (Mockup Format & Live Status)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xDD0F172A),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Text(
                            text = config.packagingType.uppercase(),
                            color = Color(0xFFF8FAFC),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Surface(
                    color = Color(0xDD0F172A),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentFoil.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = perspective.label.uppercase(),
                        color = theme.accentFoil,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            // 4. Top-Right Live Dimension Pill
            if (showDimensions) {
                Surface(
                    color = Color(0xEE0284C7),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = config.packagingDimensions,
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/* ========================================================================== */
/* 1. CLAMSHELL / BLISTER PACK MOCKUP                                         */
/* ========================================================================== */
@Composable
private fun ClamshellBlisterMockup(
    config: PackagingConfiguration,
    dims: ParsedDimensions,
    theme: MockupFinishTheme,
    perspective: PackagingPerspective,
    productImageUrl: String,
    productName: String,
    showDimensions: Boolean,
    modifier: Modifier = Modifier
) {
    val cardW = 160.dp
    val cardH = 260.dp

    Box(
        modifier = modifier
            .width(cardW + if (showDimensions) 50.dp else 0.dp)
            .height(cardH + if (showDimensions) 30.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dimension overlay lines (left & top)
        if (showDimensions) {
            DimensionCalloutFrame(
                width = cardW,
                height = cardH,
                hLabel = dims.height,
                wLabel = dims.width,
                dLabel = dims.depth
            )
        }

        // Cardboard Backing Card + Thermoformed Clear PET Blister
        Box(
            modifier = Modifier
                .width(cardW)
                .height(cardH)
                .shadow(12.dp, RoundedCornerShape(10.dp), spotColor = Color.Black)
                .clip(RoundedCornerShape(10.dp))
                .background(theme.cardSurface)
                .border(1.dp, theme.accentFoil.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
            // Background Texture & Printing
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Top metallic foil accent bar
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(theme.accentFoil.copy(alpha = 0.8f), theme.accentFoil.copy(alpha = 0.3f))
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(w, 4.dp.toPx())
                )

                // Heat-seal stippled crimp dots along perimeter flange
                val crimpMargin = 5.dp.toPx()
                val dotSpacing = 8.dp.toPx()
                var dx = crimpMargin
                while (dx < w - crimpMargin) {
                    drawCircle(Color.White.copy(alpha = 0.12f), radius = 1f, center = Offset(dx, crimpMargin))
                    drawCircle(Color.White.copy(alpha = 0.12f), radius = 1f, center = Offset(dx, h - crimpMargin))
                    dx += dotSpacing
                }
                var dy = crimpMargin
                while (dy < h - crimpMargin) {
                    drawCircle(Color.White.copy(alpha = 0.12f), radius = 1f, center = Offset(crimpMargin, dy))
                    drawCircle(Color.White.copy(alpha = 0.12f), radius = 1f, center = Offset(w - crimpMargin, dy))
                    dy += dotSpacing
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Euro-Slot Punch Hole Tab
                EuroSlotHanger(theme = theme)

                Spacer(modifier = Modifier.height(4.dp))

                // Brand Header Section
                BrandHeaderSection(config = config, theme = theme)

                Spacer(modifier = Modifier.height(6.dp))

                // Raised 3D Thermoformed Blister Bubble Cavity
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF030712).copy(alpha = 0.75f))
                        .border(1.5.dp, Color(0xFF67E8F9).copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Blister Specular Highlights & Reflection
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val bw = size.width
                        val bh = size.height

                        // Diagonal glossy blister refraction line
                        val glossPath = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(bw * 0.45f, 0f)
                            lineTo(bw * 0.15f, bh)
                            lineTo(0f, bh)
                            close()
                        }
                        drawPath(glossPath, brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.18f), Color.Transparent)))

                        // Bottom curved rim reflection
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.08f),
                            topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                            size = Size(bw - 6.dp.toPx(), bh - 6.dp.toPx()),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Stroke(width = 1f)
                        )
                    }

                    // Authentic Jig/Lure inside the cavity
                    AsyncImage(
                        model = productImageUrl,
                        contentDescription = productName,
                        contentScale = ContentScale.Fit,
                        error = painterResource(id = R.drawable.ic_jig_icon),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .shadow(8.dp, RoundedCornerShape(6.dp), spotColor = Color.Black)
                    )

                    // Blister Badge Overlay
                    Surface(
                        color = Color(0xCC000000),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "THERMOFORMED PET • 0.5MM",
                            color = Color(0xFF38BDF8),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Product Specs & Retail Barcode Bar
                RetailSpecBarcodeFooter(config = config, theme = theme, dims = dims)
            }
        }
    }
}

/* ========================================================================== */
/* 2. HEADER CARD / POLYBAG MOCKUP                                            */
/* ========================================================================== */
@Composable
private fun HeaderCardPolybagMockup(
    config: PackagingConfiguration,
    dims: ParsedDimensions,
    theme: MockupFinishTheme,
    perspective: PackagingPerspective,
    productImageUrl: String,
    productName: String,
    showDimensions: Boolean,
    modifier: Modifier = Modifier
) {
    val cardW = 164.dp
    val totalH = 270.dp

    Box(
        modifier = modifier
            .width(cardW + if (showDimensions) 50.dp else 0.dp)
            .height(totalH + if (showDimensions) 30.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showDimensions) {
            DimensionCalloutFrame(
                width = cardW,
                height = totalH,
                hLabel = dims.height,
                wLabel = dims.width,
                dLabel = "12 mm"
            )
        }

        Column(
            modifier = Modifier
                .width(cardW)
                .height(totalH)
                .shadow(14.dp, RoundedCornerShape(4.dp), spotColor = Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // A. Fold-Over Cardboard Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(theme.cardSurface)
                    .border(1.dp, theme.accentFoil.copy(alpha = 0.5f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Euro-Slot with 2 Metallic Industrial Staples
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left staple
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(2.dp)
                                .background(Color(0xFFCBD5E1))
                                .border(0.5.dp, Color(0xFF64748B))
                        )

                        EuroSlotHanger(theme = theme, modifier = Modifier.height(14.dp))

                        // Right staple
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(2.dp)
                                .background(Color(0xFFCBD5E1))
                                .border(0.5.dp, Color(0xFF64748B))
                        )
                    }

                    // Brand & Product Title on Header Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = config.companyName.take(16).uppercase(),
                                color = theme.textColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.4.sp
                            )
                            Text(
                                text = config.productName.take(20),
                                color = theme.accentFoil,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = config.modelNumber,
                            color = theme.subtitleColor,
                            fontSize = 6.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // B. Clear Heavy-Gauge Transparent Polybag Pouch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(Color(0x1A38BDF8))
                    .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
            ) {
                // Realistic clear plastic film wrinkles, side welds & reflections
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pw = size.width
                    val ph = size.height

                    // Subtle diagonal reflection sheen
                    val sheen = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(pw * 0.4f, 0f)
                        lineTo(pw * 0.1f, ph)
                        lineTo(0f, ph)
                        close()
                    }
                    drawPath(sheen, color = Color.White.copy(alpha = 0.07f))

                    // Heat-seal serration line at the bottom
                    val bY = ph - 6.dp.toPx()
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = Offset(0f, bY),
                        end = Offset(pw, bY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                }

                // Jig / Lure suspended naturally inside the transparent polybag
                AsyncImage(
                    model = productImageUrl,
                    contentDescription = productName,
                    contentScale = ContentScale.Fit,
                    error = painterResource(id = R.drawable.ic_jig_icon),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .shadow(4.dp, spotColor = Color.Black)
                )

                // Warning & Eco Imprint at bottom of polybag
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "♻ LDPE 04 RECYCLABLE • ${dims.width}x${dims.height}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 5.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "QTY: ${config.targetQuantity.take(8)}",
                        color = Color(0xFF38BDF8),
                        fontSize = 5.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/* ========================================================================== */
/* 3. RETAIL HANGING BOX MOCKUP (Folded Carton with Window)                   */
/* ========================================================================== */
@Composable
private fun RetailHangingBoxMockup(
    config: PackagingConfiguration,
    dims: ParsedDimensions,
    theme: MockupFinishTheme,
    perspective: PackagingPerspective,
    productImageUrl: String,
    productName: String,
    showDimensions: Boolean,
    modifier: Modifier = Modifier
) {
    val is3D = perspective == PackagingPerspective.THREE_D
    val boxW = 155.dp
    val boxH = 265.dp
    val sideDepthW = 24.dp

    Box(
        modifier = modifier
            .width(boxW + (if (is3D) sideDepthW else 0.dp) + if (showDimensions) 55.dp else 0.dp)
            .height(boxH + if (showDimensions) 30.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showDimensions) {
            DimensionCalloutFrame(
                width = boxW,
                height = boxH,
                hLabel = dims.height,
                wLabel = dims.width,
                dLabel = dims.depth
            )
        }

        Row(
            modifier = Modifier
                .height(boxH)
                .shadow(16.dp, RoundedCornerShape(6.dp), spotColor = Color.Black),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Front Face of the Box
            Box(
                modifier = Modifier
                    .width(boxW)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.cardSurface)
                    .border(1.dp, theme.accentFoil.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Extended Rear Hanger Tab with Euro-Slot
                    EuroSlotHanger(theme = theme)

                    Spacer(modifier = Modifier.height(4.dp))

                    // Brand Header
                    BrandHeaderSection(config = config, theme = theme)

                    Spacer(modifier = Modifier.height(6.dp))

                    // Clear Acetate Cutout Window with Drop Depth
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF030712))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner box shadow
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            // Drop shadow along top & left window cutout edge
                            drawRect(
                                brush = Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent), endY = 16.dp.toPx())
                            )
                            drawRect(
                                brush = Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent), endX = 14.dp.toPx())
                            )
                        }

                        // Jig inside the box window
                        AsyncImage(
                            model = productImageUrl,
                            contentDescription = productName,
                            contentScale = ContentScale.Fit,
                            error = painterResource(id = R.drawable.ic_jig_icon),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        // Clear Window Indicator Tag
                        Surface(
                            color = Color(0xCC0F172A),
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "DIE-CUT PET WINDOW",
                                color = Color(0xFF94A3B8),
                                fontSize = 5.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Spec Footer & Barcode
                    RetailSpecBarcodeFooter(config = config, theme = theme, dims = dims)
                }
            }

            // In 3D Isometric View: Render Shaded Side Panel
            if (is3D) {
                Box(
                    modifier = Modifier
                        .width(sideDepthW)
                        .fillMaxHeight(0.96f)
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    theme.cardSurface.copy(alpha = 0.85f),
                                    Color(0xFF030712)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "7HOOKS TACKLE • ${config.modelNumber} • ${dims.depth}",
                        color = theme.accentFoil.copy(alpha = 0.8f),
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.rotate(90f)
                    )
                }
            }
        }
    }
}

/* ========================================================================== */
/* 4. RIGID GIFT BOX MOCKUP (Luxury EVA Foam Insert)                          */
/* ========================================================================== */
@Composable
private fun RigidGiftBoxMockup(
    config: PackagingConfiguration,
    dims: ParsedDimensions,
    theme: MockupFinishTheme,
    perspective: PackagingPerspective,
    productImageUrl: String,
    productName: String,
    showDimensions: Boolean,
    modifier: Modifier = Modifier
) {
    val boxW = 200.dp
    val boxH = 240.dp

    Box(
        modifier = modifier
            .width(boxW + if (showDimensions) 55.dp else 0.dp)
            .height(boxH + if (showDimensions) 30.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showDimensions) {
            DimensionCalloutFrame(
                width = boxW,
                height = boxH,
                hLabel = dims.height,
                wLabel = dims.width,
                dLabel = dims.depth
            )
        }

        // Luxury Rigid Setup Box with Dense EVA Foam Base
        Box(
            modifier = Modifier
                .width(boxW)
                .height(boxH)
                .shadow(20.dp, RoundedCornerShape(8.dp), spotColor = Color.Black)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141416))
                .border(1.5.dp, Color(0xFFD97706).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hot-Stamped Foil Presentation Plate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "7HOOKS SIGNATURE SERIES",
                            color = Color(0xFFF59E0B),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = config.companyName.uppercase(),
                            color = Color(0xFFFFFBEB),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = Color(0x33F59E0B),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                    ) {
                        Text(
                            text = "PRO EDITION",
                            color = Color(0xFFF59E0B),
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Charcoal EVA Foam Tray with Contoured Laser Cutout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0D0D10))
                        .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Textured EVA Foam Texture & Deep Recessed Silhouette
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val fw = size.width
                        val fh = size.height

                        // Inner recessed silhouette shadow
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                            size = Size(fw - 20.dp.toPx(), fh - 20.dp.toPx()),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Fill
                        )
                        drawRoundRect(
                            color = Color(0xFF3F3F46).copy(alpha = 0.4f),
                            topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                            size = Size(fw - 20.dp.toPx(), fh - 20.dp.toPx()),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // Authentic Jig seated flush inside the custom foam cavity
                    AsyncImage(
                        model = productImageUrl,
                        contentDescription = productName,
                        contentScale = ContentScale.Fit,
                        error = painterResource(id = R.drawable.ic_jig_icon),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    )

                    Surface(
                        color = Color(0xEE18181B),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "CUSTOM LASER-CUT HIGH DENSITY EVA FOAM",
                            color = Color(0xFFD4D4D8),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Luxury Footer Plate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SKU: ${config.modelNumber} • ${dims.height}x${dims.width}x${dims.depth}",
                        color = Color(0xFFA1A1AA),
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "RIGID 1200 GSM PAPERBOARD",
                        color = Color(0xFFF59E0B),
                        fontSize = 6.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/* ========================================================================== */
/* 5. BULK OEM MASTER PACK MOCKUP (Shipping Carton with Dividers)             */
/* ========================================================================== */
@Composable
private fun BulkOemMasterPackMockup(
    config: PackagingConfiguration,
    dims: ParsedDimensions,
    theme: MockupFinishTheme,
    perspective: PackagingPerspective,
    productImageUrl: String,
    showDimensions: Boolean,
    modifier: Modifier = Modifier
) {
    val cartonW = 210.dp
    val cartonH = 240.dp

    Box(
        modifier = modifier
            .width(cartonW + if (showDimensions) 55.dp else 0.dp)
            .height(cartonH + if (showDimensions) 30.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showDimensions) {
            DimensionCalloutFrame(
                width = cartonW,
                height = cartonH,
                hLabel = dims.height,
                wLabel = dims.width,
                dLabel = dims.depth
            )
        }

        // Heavy Kraft Corrugated Shipping Carton
        Box(
            modifier = Modifier
                .width(cartonW)
                .height(cartonH)
                .shadow(16.dp, RoundedCornerShape(6.dp), spotColor = Color.Black)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFB88E5D)) // Corrugated Kraft Cardboard
                .border(1.5.dp, Color(0xFF7A5832), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Industrial Stencil Marking
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "7HOOKS COMMERCIAL OEM",
                            color = Color(0xFF261509),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "MASTER SHIPPER CARTON",
                            color = Color(0xFF4A321E),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // ISO Fragility & Handling Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "⬆⬆", fontSize = 12.sp, color = Color(0xFF261509), fontWeight = FontWeight.Bold)
                        Text(text = "🍷", fontSize = 10.sp)
                        Text(text = "☂", fontSize = 10.sp, color = Color(0xFF261509))
                    }
                }

                // Compartment Grid Divider (Egg-crate divider holding tackle units)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF8F6739))
                        .border(1.dp, Color(0xFF634320), RoundedCornerShape(4.dp))
                        .padding(4.dp)
                ) {
                    // Draw cell partitions
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        // Vertical dividers
                        drawLine(Color(0xFF4A321E), Offset(w * 0.33f, 0f), Offset(w * 0.33f, h), strokeWidth = 2.dp.toPx())
                        drawLine(Color(0xFF4A321E), Offset(w * 0.66f, 0f), Offset(w * 0.66f, h), strokeWidth = 2.dp.toPx())
                        // Horizontal divider
                        drawLine(Color(0xFF4A321E), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 2.dp.toPx())
                    }

                    // Jigs packed into compartments
                    Row(modifier = Modifier.fillMaxSize()) {
                        for (i in 0..2) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = productImageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    error = painterResource(id = R.drawable.ic_jig_icon),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }

                // White Logistics Shipping Label
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MODEL: ${config.modelNumber}",
                                color = Color(0xFF0F172A),
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "QTY: ${config.targetQuantity} • ${config.packagingDimensions}",
                                color = Color(0xFF334155),
                                fontSize = 6.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Mini Barcode
                        Column(horizontalAlignment = Alignment.End) {
                            Row(horizontalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.height(14.dp)) {
                                val bars = listOf(2, 1, 3, 1, 2, 4, 1, 2, 3, 1, 2)
                                bars.forEach { b ->
                                    Box(
                                        modifier = Modifier
                                            .width(b.dp)
                                            .fillMaxHeight()
                                            .background(Color.Black)
                                    )
                                }
                            }
                            Text(text = "OEM-MASTER", fontSize = 5.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

/* ========================================================================== */
/* 6. FLAT DIELINE BLUEPRINT VIEW (Unfolded Technical Die-Cut)                */
/* ========================================================================== */
@Composable
private fun DielineBlueprintView(
    config: PackagingConfiguration,
    dims: ParsedDimensions,
    theme: MockupFinishTheme,
    showDimensions: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF021329))
            .border(1.dp, Color(0xFF0E3A68).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Cyan Blueprint Technical Frame
            drawRect(
                color = Color(0xFF0369A1).copy(alpha = 0.3f),
                style = Stroke(width = 1f)
            )

            // Dieline Panels (Front, Back, Sides, Glue Flap, Tuck Flaps)
            val panelH = h * 0.58f
            val startY = h * 0.22f

            val glueW = w * 0.08f
            val backW = w * 0.26f
            val side1W = w * 0.16f
            val frontW = w * 0.26f
            val side2W = w * 0.16f

            var curX = (w - (glueW + backW + side1W + frontW + side2W)) / 2f

            // 1. Glue Flap
            drawRect(Color(0x330284C7), Offset(curX, startY), Size(glueW, panelH))
            drawRect(Color(0xFF38BDF8), Offset(curX, startY), Size(glueW, panelH), style = Stroke(width = 1f))
            curX += glueW

            // 2. Back Panel
            drawRect(Color(0x220284C7), Offset(curX, startY), Size(backW, panelH))
            drawRect(Color(0xFF38BDF8), Offset(curX, startY), Size(backW, panelH), style = Stroke(width = 1.5f))
            // Score crease lines
            drawLine(Color(0xFFFF70A6), Offset(curX, startY), Offset(curX, startY + panelH), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
            curX += backW

            // 3. Side Panel 1
            drawRect(Color(0x220284C7), Offset(curX, startY), Size(side1W, panelH))
            drawRect(Color(0xFF38BDF8), Offset(curX, startY), Size(side1W, panelH), style = Stroke(width = 1.5f))
            drawLine(Color(0xFFFF70A6), Offset(curX, startY), Offset(curX, startY + panelH), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
            curX += side1W

            // 4. Front Panel (with die-cut window cutout)
            drawRect(Color(0x220284C7), Offset(curX, startY), Size(frontW, panelH))
            drawRect(Color(0xFF38BDF8), Offset(curX, startY), Size(frontW, panelH), style = Stroke(width = 1.5f))
            drawLine(Color(0xFFFF70A6), Offset(curX, startY), Offset(curX, startY + panelH), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))

            // Window cutout in front panel
            val winW = frontW * 0.7f
            val winH = panelH * 0.6f
            val winX = curX + (frontW - winW) / 2f
            val winY = startY + (panelH - winH) / 2f
            drawRoundRect(
                color = Color(0xFF67E8F9),
                topLeft = Offset(winX, winY),
                size = Size(winW, winH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = 1.5f)
            )

            curX += frontW

            // 5. Side Panel 2
            drawRect(Color(0x220284C7), Offset(curX, startY), Size(side2W, panelH))
            drawRect(Color(0xFF38BDF8), Offset(curX, startY), Size(side2W, panelH), style = Stroke(width = 1.5f))
            drawLine(Color(0xFFFF70A6), Offset(curX, startY), Offset(curX, startY + panelH), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))

            // Top Hanger Extended Tab with Euro-slot
            val hangX = curX - frontW + (frontW - frontW * 0.6f) / 2f
            val hangY = startY - 26.dp.toPx()
            drawRoundRect(
                color = Color(0xFF38BDF8),
                topLeft = Offset(hangX, hangY),
                size = Size(frontW * 0.6f, 26.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = 1.5f)
            )
        }

        // Dieline Blueprint Labels
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TECHNICAL DIELINE BLUEPRINT • 1:1 CAD UNFOLD",
                        color = Color(0xFF38BDF8),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MODEL: ${config.modelNumber} • STOCK: ${config.cardStock}",
                        color = Color(0xFF94A3B8),
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // CMYK Calibration Swatches
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    listOf(Color(0xFF00FFFF), Color(0xFFFF00FF), Color(0xFFFFFF00), Color.Black).forEach { c ->
                        Box(modifier = Modifier.size(10.dp).background(c).border(0.5.dp, Color.White))
                    }
                }
            }

            // Legend / Color Key
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "— CUT LINE", color = Color(0xFF38BDF8), fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                    Text(text = "- - FOLD / SCORE", color = Color(0xFFFF70A6), fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                    Text(text = "□ DIE-CUT WINDOW", color = Color(0xFF67E8F9), fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "DIMENSIONS: ${dims.height} x ${dims.width} x ${dims.depth}",
                    color = Color.White,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* ========================================================================== */
/* REUSABLE UI BLOCKS & ANNOTATIONS                                           */
/* ========================================================================== */

@Composable
private fun EuroSlotHanger(
    theme: MockupFinishTheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Punched Euro-slot shape
        Box(
            modifier = Modifier
                .width(34.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0B111E))
                .border(1.dp, Color(0xFF475569), RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun BrandHeaderSection(
    config: PackagingConfiguration,
    theme: MockupFinishTheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "7HOOKS",
                color = theme.textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "COMMERCIAL TACKLE",
                color = theme.accentFoil,
                fontSize = 6.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }

        // Custom Distributor Logo or Company Name Pill
        if (config.customLogoUri != null) {
            AsyncImage(
                model = Uri.parse(config.customLogoUri),
                contentDescription = "Distributor Logo",
                modifier = Modifier
                    .size(34.dp, 18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White)
                    .padding(2.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Surface(
                color = if (theme.isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(3.dp),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, theme.accentFoil.copy(alpha = 0.4f))
            ) {
                Text(
                    text = config.companyName.take(14).uppercase(),
                    color = theme.accentFoil,
                    fontSize = 6.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RetailSpecBarcodeFooter(
    config: PackagingConfiguration,
    theme: MockupFinishTheme,
    dims: ParsedDimensions
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Product Line & Model SKU
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = config.productName.take(22),
                color = theme.textColor,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = config.modelNumber,
                color = theme.accentFoil,
                fontSize = 6.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // High-density Retail Barcode Footer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White)
                .padding(horizontal = 5.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated Barcode Lines
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(14.dp)
            ) {
                val widths = listOf(2, 1, 3, 1, 2, 3, 1, 2, 1, 4, 2, 1, 2, 3, 1, 2)
                widths.forEach { bw ->
                    Box(
                        modifier = Modifier
                            .width(bw.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF0F172A))
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${dims.height}x${dims.width}",
                    color = Color(0xFF0F172A),
                    fontSize = 5.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "QTY: ${config.targetQuantity.take(6)}",
                    color = Color(0xFF64748B),
                    fontSize = 5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Clean architectural dimension callout frame drawing height and width leader lines.
 */
@Composable
private fun DimensionCalloutFrame(
    width: Dp,
    height: Dp,
    hLabel: String,
    wLabel: String,
    dLabel: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Vertical Height Dimension (Right side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ArrowDropUp, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
            Surface(
                color = Color(0xEE0284C7),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "H: $hLabel",
                    color = Color.White,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
        }

        // Horizontal Width Dimension (Bottom side)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ArrowLeft, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
            Surface(
                color = Color(0xEE0284C7),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "W: $wLabel • D: $dLabel",
                    color = Color.White,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Icon(Icons.Default.ArrowRight, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
        }
    }
}

/* ========================================================================== */
/* CANVAS BACKGROUND DRAWING HELPERS                                          */
/* ========================================================================== */

private fun DrawScope.drawIsometricSurfaceGrid(size: Size) {
    val spacing = 24.dp.toPx()
    val lineColor = Color(0x120284C7)
    var x = 0f
    while (x <= size.width) {
        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += spacing
    }
    var y = 0f
    while (y <= size.height) {
        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += spacing
    }
}

private fun DrawScope.drawStudioPhotographyLighting(size: Size) {
    // Subtle circular specular highlight behind product
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.White.copy(alpha = 0.07f), Color.Transparent),
            center = Offset(size.width * 0.5f, size.height * 0.45f),
            radius = size.width * 0.4f
        )
    )
}

private fun DrawScope.drawDielineBlueprintGrid(size: Size) {
    val grid = 16.dp.toPx()
    val c = Color(0x180284C7)
    var gx = 0f
    while (gx <= size.width) {
        drawLine(c, Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 1f)
        gx += grid
    }
    var gy = 0f
    while (gy <= size.height) {
        drawLine(c, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1f)
        gy += grid
    }
}

private fun DrawScope.drawRetailPegboardBackground(size: Size) {
    // Perforated pegboard holes in regular matrix
    val pegHoleRadius = 2.5.dp.toPx()
    val pegSpacing = 22.dp.toPx()
    val holeColor = Color(0xFF030712)
    val ringColor = Color(0xFF334155).copy(alpha = 0.5f)

    var py = 12.dp.toPx()
    while (py <= size.height) {
        var px = 12.dp.toPx()
        while (px <= size.width) {
            drawCircle(holeColor, radius = pegHoleRadius, center = Offset(px, py))
            drawCircle(ringColor, radius = pegHoleRadius + 0.5f, center = Offset(px, py), style = Stroke(width = 0.8f))
            px += pegSpacing
        }
        py += pegSpacing
    }

    // Chrome Peg Hook Bar extending over center
    val hookStartX = size.width * 0.5f
    val hookY = size.height * 0.12f
    drawLine(
        color = Color(0xFFE2E8F0),
        start = Offset(hookStartX, hookY - 14.dp.toPx()),
        end = Offset(hookStartX, hookY + 8.dp.toPx()),
        strokeWidth = 3.dp.toPx()
    )
    drawCircle(Color(0xFF94A3B8), radius = 3.dp.toPx(), center = Offset(hookStartX, hookY + 8.dp.toPx()))
}

data class ParsedDimensions(
    val height: String,
    val width: String,
    val depth: String
)

private fun parsePackagingDimensions(dims: String): ParsedDimensions {
    val clean = dims.replace("mm", "", ignoreCase = true).trim()
    val parts = clean.split(Regex("[xX×*]")).map { it.trim() }
    return when {
        parts.size >= 3 -> ParsedDimensions(
            height = "${parts[0]} mm",
            width = "${parts[1]} mm",
            depth = "${parts[2]} mm"
        )
        parts.size == 2 -> ParsedDimensions(
            height = "${parts[0]} mm",
            width = "${parts[1]} mm",
            depth = "25 mm"
        )
        else -> ParsedDimensions(
            height = "180 mm",
            width = "60 mm",
            depth = "30 mm"
        )
    }
}
