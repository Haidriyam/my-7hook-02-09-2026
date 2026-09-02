package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JigPatternType

data class RealisticFinish(
    val id: String,
    val name: String,
    val finishType: String,
    val baseColor: Color,
    val accentColor: Color,
    val patternType: JigPatternType,
    val description: String
)

object RealisticFinishes {
    val allFinishes = listOf(
        RealisticFinish(
            id = "orange_black",
            name = "Orange / Black",
            finishType = "High-Gloss Metallic",
            baseColor = Color(0xFFEA580C),
            accentColor = Color(0xFF0F172A),
            patternType = JigPatternType.SOLID_STRIPE,
            description = "High-vis blaze orange metallic with satin stealth-black keel."
        ),
        RealisticFinish(
            id = "yellow_dotted",
            name = "Yellow / Dotted",
            finishType = "Luminous Glow + Dots",
            baseColor = Color(0xFFEAB308),
            accentColor = Color(0xFF0284C7),
            patternType = JigPatternType.DOT_PATTERN,
            description = "Electric yellow pearl with precision deep-water strike dots."
        ),
        RealisticFinish(
            id = "yellow_orange",
            name = "Yellow / Orange",
            finishType = "Solar Pearl Fade",
            baseColor = Color(0xFFFACC15),
            accentColor = Color(0xFFEA580C),
            patternType = JigPatternType.SOLID_STRIPE,
            description = "Solar yellow base fading into fire-orange lateral strike zone."
        ),
        RealisticFinish(
            id = "candy_blue_orange",
            name = "Candy Blue / Orange",
            finishType = "Anodized Candy Clear",
            baseColor = Color(0xFF0284C7),
            accentColor = Color(0xFFF97316),
            patternType = JigPatternType.HOLOGRAPHIC_SLASH,
            description = "Translucent deep candy blue with fluorescent orange belly."
        ),
        RealisticFinish(
            id = "candy_pink_green",
            name = "Candy Pink / Green",
            finishType = "UV High-Luminescence",
            baseColor = Color(0xFFEC4899),
            accentColor = Color(0xFF10B981),
            patternType = JigPatternType.HOLOGRAPHIC_SLASH,
            description = "UV fluorescent hot pink with metallic emerald green stripe."
        ),
        RealisticFinish(
            id = "candy_yellow_black",
            name = "Candy Yellow / Black",
            finishType = "Mirror Gloss Keel",
            baseColor = Color(0xFFEAB308),
            accentColor = Color(0xFF0F172A),
            patternType = JigPatternType.SOLID_STRIPE,
            description = "High-gloss yellow dorsal plate with stealth black hydrofoil."
        ),
        RealisticFinish(
            id = "crystal_pink_blue",
            name = "Crystal Pink / Blue",
            finishType = "Prismatic Facet Flash",
            baseColor = Color(0xFFF472B6),
            accentColor = Color(0xFF38BDF8),
            patternType = JigPatternType.CRYSTAL_FACET,
            description = "Prismatic multi-faceted crystal reflecting pink & cyan hues."
        ),
        RealisticFinish(
            id = "crystal_yellow_blue",
            name = "Crystal Yellow / Blue",
            finishType = "Iridescent Gold Spine",
            baseColor = Color(0xFFFDE047),
            accentColor = Color(0xFF1D4ED8),
            patternType = JigPatternType.CRYSTAL_FACET,
            description = "Multi-angle reflection with yellow gold flash and royal spine."
        )
    )
}

@Composable
fun RealisticTextureTile(
    finish: RealisticFinish,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 76.dp
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "tileBorder"
    )

    Column(
        modifier = modifier
            .width(sizeDp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(sizeDp, sizeDp * 0.75f)
                .shadow(if (isSelected) 6.dp else 2.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRealisticFinishTexture(
                    baseColor = finish.baseColor,
                    accentColor = finish.accentColor,
                    pattern = finish.patternType
                )
            }

            if (isSelected) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = finish.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

fun DrawScope.drawRealisticFinishTexture(
    baseColor: Color,
    accentColor: Color,
    pattern: JigPatternType
) {
    val w = size.width
    val h = size.height

    // 1. Base Material Surface with Metallic Shimmer Gradient
    val baseGradient = Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.9f),
            baseColor,
            baseColor.copy(alpha = 0.85f),
            baseColor
        ),
        start = Offset(0f, 0f),
        end = Offset(w, h)
    )
    drawRect(brush = baseGradient, size = size)

    // 2. Pattern Specific Realistic Detailing
    when (pattern) {
        JigPatternType.SOLID_STRIPE -> {
            // Dark Hydro Keel or Lateral Stripe
            val stripePath = Path().apply {
                moveTo(0f, h * 0.45f)
                lineTo(w, h * 0.25f)
                lineTo(w, h * 0.75f)
                lineTo(0f, h * 0.95f)
                close()
            }
            drawPath(
                path = stripePath,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, accentColor.copy(alpha = 0.8f)),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )
        }
        JigPatternType.DOT_PATTERN -> {
            // Strike Dot Matrix
            val rows = 3
            val cols = 5
            val colStep = w / (cols + 1)
            val rowStep = h / (rows + 1)

            for (r in 1..rows) {
                for (c in 1..cols) {
                    val cx = c * colStep + (if (r % 2 == 0) colStep * 0.3f else 0f)
                    val cy = r * rowStep
                    // Outer glow
                    drawCircle(
                        color = Color.White.copy(alpha = 0.6f),
                        radius = 4f,
                        center = Offset(cx, cy)
                    )
                    // Inner dot
                    drawCircle(
                        color = accentColor,
                        radius = 2.8f,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
        JigPatternType.HOLOGRAPHIC_SLASH -> {
            // Iridescent Holographic Slashes
            val slashWidth = w * 0.15f
            for (i in 0..4) {
                val startX = i * (w * 0.25f) - w * 0.1f
                val slashPath = Path().apply {
                    moveTo(startX, 0f)
                    lineTo(startX + slashWidth, 0f)
                    lineTo(startX + slashWidth - w * 0.3f, h)
                    lineTo(startX - w * 0.3f, h)
                    close()
                }
                drawPath(
                    path = slashPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.9f),
                            Color.White.copy(alpha = 0.7f),
                            accentColor.copy(alpha = 0.9f)
                        ),
                        start = Offset(startX, 0f),
                        end = Offset(startX + slashWidth, h)
                    )
                )
            }
        }
        JigPatternType.CRYSTAL_FACET -> {
            // Prismatic Diamond & Triangular Crystal Facets
            val facetCount = 4
            val step = w / facetCount
            for (i in 0 until facetCount) {
                val fPath1 = Path().apply {
                    moveTo(i * step, 0f)
                    lineTo((i + 1) * step, 0f)
                    lineTo(i * step + step / 2, h / 2)
                    close()
                }
                val fPath2 = Path().apply {
                    moveTo(i * step, h)
                    lineTo((i + 1) * step, h)
                    lineTo(i * step + step / 2, h / 2)
                    close()
                }
                drawPath(
                    fPath1,
                    brush = Brush.linearGradient(
                        listOf(baseColor, accentColor.copy(alpha = 0.7f)),
                        Offset(i * step, 0f),
                        Offset((i + 1) * step, h / 2)
                    )
                )
                drawPath(
                    fPath2,
                    brush = Brush.linearGradient(
                        listOf(accentColor, baseColor.copy(alpha = 0.7f)),
                        Offset(i * step, h),
                        Offset((i + 1) * step, h / 2)
                    )
                )
            }
        }
    }

    // 3. Realistic Specular Light Flare & Gloss Top-Coat
    val specularGloss = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.15f),
            Color.Transparent,
            Color.White.copy(alpha = 0.25f)
        ),
        start = Offset(0f, 0f),
        end = Offset(w, h * 0.6f)
    )
    drawRect(brush = specularGloss, size = size)

    // Outer subtle bevel border
    drawRect(
        color = Color.White.copy(alpha = 0.35f),
        topLeft = Offset(0f, 0f),
        size = size,
        style = Stroke(width = 1.5f)
    )
}

@Composable
fun RealisticFinishSelector(
    selectedColorName: String,
    onSelectFinish: (RealisticFinish) -> Unit,
    modifier: Modifier = Modifier
) {
    TactileCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FINISH / MATERIAL APPEARANCE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Realistic hydrodynamic coatings & visual material textures",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Realistic Texture Tiles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RealisticFinishes.allFinishes.forEach { finish ->
                    val isSelected = selectedColorName.contains(finish.name.substringBefore(" /"), ignoreCase = true) ||
                            selectedColorName.contains(finish.id.replace("_", " "), ignoreCase = true)

                    RealisticTextureTile(
                        finish = finish,
                        isSelected = isSelected,
                        onClick = { onSelectFinish(finish) },
                        modifier = Modifier.testTag("finish_tile_${finish.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Currently Selected Texture Info Banner
            val currentFinish = RealisticFinishes.allFinishes.find {
                selectedColorName.contains(it.name.substringBefore(" /"), ignoreCase = true)
            } ?: RealisticFinishes.allFinishes.first()

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp, 28.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRealisticFinishTexture(
                                baseColor = currentFinish.baseColor,
                                accentColor = currentFinish.accentColor,
                                pattern = currentFinish.patternType
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${currentFinish.name} • ${currentFinish.finishType}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentFinish.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialAndFinishPreviewCard(
    material: String,
    finishName: String,
    baseColor: Color,
    accentColor: Color,
    pattern: JigPatternType,
    modifier: Modifier = Modifier
) {
    TactileCard(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "MATERIAL & FINISH SPECIFICATION",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Large realistic material banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRealisticFinishTexture(
                        baseColor = baseColor,
                        accentColor = accentColor,
                        pattern = pattern
                    )
                }

                // Spec overlay tag
                Surface(
                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "REALISTIC SURFACE RENDER",
                        color = Color(0xFF38BDF8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Material Alloy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(material, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hydro Coating", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(finishName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
