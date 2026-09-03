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
import androidx.compose.runtime.setValue
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
    val factoryFinishes = listOf(
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

    // EXPANDED CUSTOM APPEARANCE FINISHES (Requirement 13)
    val customAppearanceFinishes = listOf(
        RealisticFinish("gloss_orange", "Gloss Orange", "Mirror Polyurethane", Color(0xFFFF6D00), Color(0xFFFF9E40), JigPatternType.SOLID_STRIPE, "Deep mirror gloss high-saturation blaze orange."),
        RealisticFinish("matte_orange", "Matte Orange", "Anti-Glare Frosted", Color(0xFFD84315), Color(0xFFBF360C), JigPatternType.SOLID_STRIPE, "Matte anti-reflective orange for high sun visibility."),
        RealisticFinish("gloss_yellow", "Gloss Yellow", "Solar Gloss Coat", Color(0xFFFFD600), Color(0xFFFFEA00), JigPatternType.SOLID_STRIPE, "Brilliant vibrant high-gloss solar canary yellow."),
        RealisticFinish("matte_yellow", "Matte Yellow", "Satin Low-Sheen", Color(0xFFFBC02D), Color(0xFFF57F17), JigPatternType.SOLID_STRIPE, "Soft satin finish yellow body with stealth edge."),
        RealisticFinish("gloss_black", "Gloss Black", "Onyx Piano Gloss", Color(0xFF1E293B), Color(0xFF0F172A), JigPatternType.SOLID_STRIPE, "Piano black deep lacquer with obsidian reflective sheen."),
        RealisticFinish("pearl_white", "Pearl White", "Iridescent Mother-of-Pearl", Color(0xFFF8FAFC), Color(0xFFE2E8F0), JigPatternType.CRYSTAL_FACET, "Multi-chromatic pearlescent finish resembling natural scale luster."),
        RealisticFinish("pearl_blue", "Pearl Blue", "Oceanic Pearl Sparkle", Color(0xFF38BDF8), Color(0xFF0284C7), JigPatternType.CRYSTAL_FACET, "Shimmering deep sea pelagic pearl blue."),
        RealisticFinish("metallic_silver", "Metallic Silver", "Polished Chrome Flash", Color(0xFFCBD5E1), Color(0xFF94A3B8), JigPatternType.HOLOGRAPHIC_SLASH, "High-reflective mirror chrome simulating fresh baitfish flash."),
        RealisticFinish("metallic_gold", "Metallic Gold", "24K Gold Leaf Anodize", Color(0xFFEAB308), Color(0xFFCA8A04), JigPatternType.HOLOGRAPHIC_SLASH, "Rich gold anodized foil finish for stained and dark waters."),
        RealisticFinish("translucent_blue", "Translucent Blue", "Clear Tint Cyan", Color(0xFF00E5FF).copy(alpha = 0.85f), Color(0xFF0091EA), JigPatternType.CRYSTAL_FACET, "Semi-transparent clear resin showing core refraction."),
        RealisticFinish("translucent_pink", "Translucent Pink", "Ghost Coral Tint", Color(0xFFFF4081).copy(alpha = 0.85f), Color(0xFFC51162), JigPatternType.CRYSTAL_FACET, "Ghost translucent pink for high-pressure spooky fish."),
        RealisticFinish("translucent_yellow", "Translucent Yellow", "Chartreuse Amber Tint", Color(0xFFEEFF41).copy(alpha = 0.85f), Color(0xFFAEEA00), JigPatternType.CRYSTAL_FACET, "High-visibility chartreuse ghost tint for murky estuary water."),
        RealisticFinish("crystal_blue", "Crystal Blue", "Facet Diamond Blue", Color(0xFF0284C7), Color(0xFF7DD3FC), JigPatternType.CRYSTAL_FACET, "Prismatic crystalline cut with intense blue refraction."),
        RealisticFinish("crystal_pink", "Crystal Pink", "Ruby Prismatic Facet", Color(0xFFF43F5E), Color(0xFFFDA4AF), JigPatternType.CRYSTAL_FACET, "Diamond-faceted ruby pink body refracting 360-degree light."),
        RealisticFinish("holographic", "Holographic", "Rainbow Laser Sheen", Color(0xFFA855F7), Color(0xFF06B6D4), JigPatternType.HOLOGRAPHIC_SLASH, "Full-spectrum laser holographic foil that changes color at every angle."),
        RealisticFinish("clear_coat", "Clear Coat", "Optical Polycarbonate", Color(0xFFF1F5F9), Color(0xFFE2E8F0), JigPatternType.SOLID_STRIPE, "Ultra-durable scratch resistant 2K crystal clear coat."),
        RealisticFinish("smoke_tint", "Smoke Tint", "Smoked Gray Translucent", Color(0xFF475569), Color(0xFF1E293B), JigPatternType.SOLID_STRIPE, "Stealthy smoked glass appearance with dark silhouette."),
        RealisticFinish("natural_titanium", "Natural Titanium/Tungsten", "Raw Industrial Metal", Color(0xFF64748B), Color(0xFF334155), JigPatternType.SOLID_STRIPE, "Uncoated raw sintered alloy with industrial micro-brush texture.")
    )

    val allFinishes: List<RealisticFinish> = factoryFinishes + customAppearanceFinishes
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
    var selectedTab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val displayFinishes = if (selectedTab == 0) RealisticFinishes.factoryFinishes else RealisticFinishes.customAppearanceFinishes

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
                        text = "MATERIAL AND LOOK",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Realistic factory coatings & custom appearance finishes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Two distinct sections selector (Requirement 13)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                    shadowElevation = if (selectedTab == 0) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { selectedTab = 0 }
                ) {
                    Text(
                        text = "Available Product Finish",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Surface(
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                    shadowElevation = if (selectedTab == 1) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { selectedTab = 1 }
                ) {
                    Text(
                        text = "Custom Appearance",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                displayFinishes.forEach { finish ->
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
