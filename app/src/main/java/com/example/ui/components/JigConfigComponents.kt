package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JigProduct
import com.example.data.model.ProductConfiguration
import java.util.Locale

/**
 * Data class representing the result of hydrodynamic & structural validation
 */
data class JigValidationResult(
    val isValid: Boolean,
    val isSevere: Boolean,
    val statusBadge: String,
    val title: String,
    val message: String,
    val recommendation: String,
    val goldenWeight: Float,
    val goldenLength: Float,
    val goldenWidth: Float
)

/**
 * Verifies if the combination of weight, length, and width is hydrodynamically & structurally valid.
 * Triggers warnings if proportions will cause body breakage or deadweight drag failure.
 */
fun validateJigParameters(
    weight: Float,
    length: Float,
    width: Float,
    selectedJig: JigProduct
): JigValidationResult {
    val slenderness = length / width.coerceAtLeast(1f)

    return when {
        // Severe: Extremely elongated & thin -> structural snap / bending hazard under strike load
        slenderness > 9.5f -> {
            val recWidth = (length / 5.5f).coerceIn(selectedJig.minWidthMm, selectedJig.maxWidthMm)
            JigValidationResult(
                isValid = false,
                isSevere = true,
                statusBadge = "STRUCTURAL BREAKAGE HAZARD",
                title = "Critical Slenderness: Body Fracture Risk",
                message = "The ratio of Length (${length.toInt()} mm) to Width (${width.toInt()} mm) is ${String.format(Locale.US, "%.1f", slenderness)}:1 (Max allowable: 8.5:1). Under pelagic strike torque or deep-sea water pressure, this ultra-thin core will bend, fracture, or crack!",
                recommendation = "Increase hydrodynamic width to ${recWidth.toInt()} mm to restore structural integrity.",
                goldenWeight = (length * 0.55f).coerceIn(selectedJig.minWeightGrams, selectedJig.maxWeightGrams),
                goldenLength = length,
                goldenWidth = recWidth
            )
        }
        // Severe: Extremely heavy in a tiny short body -> hydrodynamic stone deadweight
        weight > 120f && length < 90f -> {
            JigValidationResult(
                isValid = false,
                isSevere = true,
                statusBadge = "HYDRODYNAMIC DEADWEIGHT FAILURE",
                title = "Excessive Mass: Zero Strike Flutter",
                message = "Concentrated mass of ${weight.toInt()} g in a short ${length.toInt()} mm body creates an aerodynamic plummet. The lure will drop like a dead weight with zero flutter, roll, or strike trigger.",
                recommendation = "Extend length to at least 130 mm or reduce mass to ~50 g for proper hydro-keel planing.",
                goldenWeight = 50f,
                goldenLength = 115f,
                goldenWidth = 22f
            )
        }
        // Severe: Large body with tiny weight -> drift stall and flutter stall
        weight < 35f && length > 165f -> {
            JigValidationResult(
                isValid = false,
                isSevere = true,
                statusBadge = "AERODYNAMIC DRIFT COLLAPSE",
                title = "Insufficient Ballast: High Drift Stall",
                message = "At ${length.toInt()} mm length, a mass of only ${weight.toInt()} g lacks core ballast. Currents will tumble the jig without sinking, and body casting stability will collapse.",
                recommendation = "Increase mass to at least 80 g for balanced oceanic penetration.",
                goldenWeight = 80f,
                goldenLength = length,
                goldenWidth = width
            )
        }
        // Moderate: Too stubby & wide
        slenderness < 2.9f -> {
            val recWidth = (length / 5.2f).coerceIn(selectedJig.minWidthMm, selectedJig.maxWidthMm)
            JigValidationResult(
                isValid = false,
                isSevere = false,
                statusBadge = "EXCESSIVE DRAG WARNING",
                title = "Excessive Width: Sub-optimal Drag",
                message = "Width of ${width.toInt()} mm creates excessive vertical water resistance. Fast-pitch retrieves will be exhausting, and line tension will overpower the rod tip.",
                recommendation = "Narrow hydrodynamic width to ${recWidth.toInt()} mm.",
                goldenWeight = weight,
                goldenLength = length,
                goldenWidth = recWidth
            )
        }
        // Valid & Optimal
        else -> {
            JigValidationResult(
                isValid = true,
                isSevere = false,
                statusBadge = "HYDRODYNAMICALLY BALANCED",
                title = "Optimal Competition Geometry",
                message = "Dimensions are perfectly balanced for high-action pelagic flutter and responsive darting.",
                recommendation = "Meets 7Hooks factory specifications for structural strength and erratic flutter.",
                goldenWeight = weight,
                goldenLength = length,
                goldenWidth = width
            )
        }
    }
}

/**
 * Validation Warning & Breakage Component
 * Prominently alerts the user if combination is invalid, displaying broken/cracked visual indicator
 * and offering 1-tap Auto-Calibration to the golden ratio!
 */
@Composable
fun JigValidationCard(
    validation: JigValidationResult,
    onAutoCalibrate: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!validation.isValid) {
        val hazardColor = if (validation.isSevere) Color(0xFFEF4444) else Color(0xFFF59E0B)
        val containerBg = if (validation.isSevere) Color(0xFF450A0A).copy(alpha = 0.35f) else Color(0xFF451A03).copy(alpha = 0.35f)

        Surface(
            color = containerBg,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, hazardColor),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = hazardColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = validation.statusBadge,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = hazardColor,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Broken / Cracked Graphic Badge
                    Surface(
                        color = hazardColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, hazardColor)
                    ) {
                        Text(
                            text = if (validation.isSevere) "CRACK RISK" else "UNBALANCED",
                            color = hazardColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = validation.title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = validation.message,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = validation.recommendation,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = hazardColor,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onAutoCalibrate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = hazardColor,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto-Calibrate",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    } else {
        // Balanced Green Pill
        Surface(
            color = Color(0xFF065F46).copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "HYDRODYNAMICALLY BALANCED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        letterSpacing = 0.4.sp
                    )
                }

                Text(
                    text = "Ratio verified: Pelagic Flutter Active",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Data Model for Best Combination Preset
 */
data class BestCombinationPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val targetSpecies: String,
    val targetDepth: String,
    val weightGrams: Float,
    val lengthMm: Float,
    val widthMm: Float,
    val finishType: String,
    val frontRing: String,
    val backRing: String,
    val hookType: String,
    val eyeStyle: String,
    val assistCord: String,
    val flutterAction: String
)

/**
 * Best Combinations Tab Content
 * Presents curated, mathematically balanced factory setups optimized for specific fishing CUJs
 */
@Composable
fun BestCombinationsSection(
    config: ProductConfiguration,
    selectedJig: JigProduct,
    onApplyPreset: (BestCombinationPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = remember(selectedJig.id) {
        listOf(
            BestCombinationPreset(
                id = "preset_pelagic_speed",
                title = "Pelagic Speed Darting",
                subtitle = "Deep Water Flutter • Fast Pitch",
                targetSpecies = "Yellowtail, Kingfish, Amberjack, Tuna",
                targetDepth = "40 – 100 meters (Thermocline Penetration)",
                weightGrams = 60f.coerceIn(selectedJig.minWeightGrams, selectedJig.maxWeightGrams),
                lengthMm = 120f.coerceIn(selectedJig.minLengthMm, selectedJig.maxLengthMm),
                widthMm = 18f.coerceIn(selectedJig.minWidthMm, selectedJig.maxWidthMm),
                finishType = "High-Gloss Metallic",
                frontRing = "Heavy Duty",
                backRing = "Standard",
                hookType = "Single Saltwater Assist 4/0",
                eyeStyle = "3D Luminous Strike Eye",
                assistCord = "Braided PE (200 lb)",
                flutterAction = "Tight spiral flutter with razor-sharp lateral darting on high-speed lift."
            ),
            BestCombinationPreset(
                id = "preset_slow_pitch_reef",
                title = "Slow Pitch Reef Tactical",
                subtitle = "Demersal Flutter • Fall Trigger",
                targetSpecies = "Snapper, Grouper, Coral Trout, Cod",
                targetDepth = "20 – 60 meters (Drop-Offs & Coral Pinnacles)",
                weightGrams = 80f.coerceIn(selectedJig.minWeightGrams, selectedJig.maxWeightGrams),
                lengthMm = 135f.coerceIn(selectedJig.minLengthMm, selectedJig.maxLengthMm),
                widthMm = 26f.coerceIn(selectedJig.minWidthMm, selectedJig.maxWidthMm),
                finishType = "Luminous Glow",
                frontRing = "Heavy Duty",
                backRing = "Heavy Duty",
                hookType = "Twin Assist Hooks 3/0",
                eyeStyle = "Holographic Foil Eye",
                assistCord = "Kevlar Core (250 lb)",
                flutterAction = "Wide horizontal wobble on the pause simulating wounded baitfish dying fall."
            ),
            BestCombinationPreset(
                id = "preset_heavy_drift_offshore",
                title = "Heavy Oceanic Drift & Current",
                subtitle = "Extreme Depth • High Density Stiff Keel",
                targetSpecies = "Dogtooth Tuna, Giant Trevally, Marlin",
                targetDepth = "80 – 160 meters (Strong Ocean Rip Currents)",
                weightGrams = 120f.coerceIn(selectedJig.minWeightGrams, selectedJig.maxWeightGrams),
                lengthMm = 160f.coerceIn(selectedJig.minLengthMm, selectedJig.maxLengthMm),
                widthMm = 22f.coerceIn(selectedJig.minWidthMm, selectedJig.maxWidthMm),
                finishType = "Holographic Flash",
                frontRing = "Heavy Duty",
                backRing = "Heavy Duty",
                hookType = "Heavy Duty Forged 5/0",
                eyeStyle = "High-Contrast Target Eye",
                assistCord = "Wire Assist (150 lb)",
                flutterAction = "Streamlined high-density descent cutting cleanly through multi-layered thermoclines."
            ),
            BestCombinationPreset(
                id = "preset_shore_casting",
                title = "Shore Casting & Inshore Light",
                subtitle = "Distance Aerodynamics • Surface Skip",
                targetSpecies = "Mackerel, Bonito, Queenfish, Tailor",
                targetDepth = "5 – 30 meters (Headlands, Rocks, Beaches)",
                weightGrams = 40f.coerceIn(selectedJig.minWeightGrams, selectedJig.maxWeightGrams),
                lengthMm = 95f.coerceIn(selectedJig.minLengthMm, selectedJig.maxLengthMm),
                widthMm = 18f.coerceIn(selectedJig.minWidthMm, selectedJig.maxWidthMm),
                finishType = "UV Reactive",
                frontRing = "Standard",
                backRing = "None",
                hookType = "Single Saltwater Assist 2/0",
                eyeStyle = "3D Luminous Strike Eye",
                assistCord = "None (Optional / Unrigged)",
                flutterAction = "Maximized casting distance with responsive skipping action along white-water breaks."
            )
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "ENGINEERED BEST COMBINATIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Factory-calibrated setups matching mass, length, width, and rigging for guaranteed hydro-balance.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        presets.forEach { preset ->
            val isCurrentMatch = config.weightGrams == preset.weightGrams &&
                    config.lengthMm == preset.lengthMm &&
                    config.widthMm == preset.widthMm

            Surface(
                color = if (isCurrentMatch) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isCurrentMatch) 1.5.dp else 0.8.dp,
                    color = if (isCurrentMatch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                shadowElevation = 1.5.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = preset.subtitle,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (isCurrentMatch) {
                            Surface(
                                color = Color(0xFF10B981),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE SETUP",
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Spec Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${preset.weightGrams.toInt()} g",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${preset.lengthMm.toInt()} mm × ${preset.widthMm.toInt()} mm",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = preset.finishType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Target Species: ${preset.targetSpecies}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Hydro Action: ${preset.flutterAction}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onApplyPreset(preset) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrentMatch) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                            contentColor = if (isCurrentMatch) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurrentMatch) Icons.Default.Check else Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCurrentMatch) "Applied to Configurator" else "Apply This Best Combination",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3D Eye Color Specimen & Preview Component
 * Displays live visual eye preview with pupil and colored strike rim when selecting 3D strike eyes
 */
@Composable
fun StrikeEyePreviewBox(
    eyeStyle: String,
    selectedEyeColorName: String,
    onSelectEyeColor: (String, Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val eyeColorOptions = listOf(
        Pair("Luminous Lime", Color(0xFF4ADE80)),
        Pair("Apex Red", Color(0xFFEF4444)),
        Pair("Ocean Blue", Color(0xFF38BDF8)),
        Pair("Solar Gold", Color(0xFFFBBF24)),
        Pair("Holographic Silver", Color(0xFFE2E8F0)),
        Pair("Violet UV", Color(0xFFA855F7))
    )

    val activeColor = eyeColorOptions.find { it.first == selectedEyeColorName }?.second ?: Color(0xFF4ADE80)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Eye Preview Graphic (Convex Dome + Iris + Predatory Pupil + Specular Reflection)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(activeColor, activeColor.copy(alpha = 0.6f), Color(0xFF0F172A))
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Outer eye ring
                            drawCircle(
                                color = activeColor,
                                radius = w * 0.44f,
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            // Predatory Pupil (Black Oval)
                            drawOval(
                                color = Color(0xFF0F172A),
                                topLeft = Offset(w * 0.32f, h * 0.22f),
                                size = Size(w * 0.36f, h * 0.56f)
                            )

                            // Specular Light Glint Reflection
                            drawCircle(
                                color = Color.White,
                                radius = 2.2.dp.toPx(),
                                center = Offset(w * 0.40f, h * 0.32f)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "EYE PREVIEW: $eyeStyle",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = "Ø 7.5 mm Domed Lens • ${selectedEyeColorName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    color = activeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, activeColor)
                ) {
                    Text(
                        text = "UV STRIKE TRIGGER",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Eye Color Swatches Row
            Text(
                text = "Iris Color Trigger:",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                eyeColorOptions.forEach { (name, color) ->
                    val isSelected = selectedEyeColorName == name
                    Surface(
                        onClick = { onSelectEyeColor(name, color) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.2.dp else 0.8.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .background(color, CircleShape)
                                    .border(0.5.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                            )
                            Text(
                                text = name,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data Model for Term Info
 */
data class TechnicalTermInfo(
    val title: String,
    val definition: String,
    val engineeringRole: String
)

/**
 * Small Info 'i' Button to attach next to any technical word
 */
@Composable
fun TechnicalInfoIcon(
    term: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info about $term",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(13.dp)
        )
    }
}

/**
 * Convenient TechnicalInfoIcon overload that builds TechnicalTermInfo directly
 */
@Composable
fun TechnicalInfoIcon(
    term: String,
    definition: String,
    recommendation: String = "",
    onShowInfo: (TechnicalTermInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    TechnicalInfoIcon(
        term = term,
        onClick = {
            onShowInfo(
                TechnicalTermInfo(
                    title = term,
                    definition = definition,
                    engineeringRole = recommendation
                )
            )
        },
        modifier = modifier
    )
}

/**
 * Technical Term Information Dialog explaining what each word stands for
 */
@Composable
fun TechnicalTermDialog(
    termInfo: TechnicalTermInfo?,
    onDismiss: () -> Unit
) {
    if (termInfo != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = termInfo.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = termInfo.definition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "FUNCTION & APPLICATION:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = termInfo.engineeringRole,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
