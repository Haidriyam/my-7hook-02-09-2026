package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductCatalog
import com.example.ui.components.*
import com.example.viewmodel.ConfiguratorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LureConfigScreen(
    configViewModel: ConfiguratorViewModel,
    onNavigateToEngineering: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentConfig by configViewModel.currentConfig.collectAsState()
    val selectedLure by configViewModel.selectedLureProduct.collectAsState()

    Scaffold(
        topBar = {
            AppHeader(
                title = "Technical Specifications",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${currentConfig.lengthMm.toInt()}mm • ${String.format(java.util.Locale.US, "%.1f", currentConfig.weightGrams)}g • ${String.format(java.util.Locale.US, "%.1f", currentConfig.divingDepthMeters)}m",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Model: ${currentConfig.modelNumber} • ${currentConfig.buoyancy}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    TactileButton(
                        onClick = onNavigateToEngineering,
                        variant = TactileButtonVariant.PRIMARY,
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        text = "Technical Drawing",
                        testTag = "lure_continue_to_engineering_button"
                    )
                }
            }
        },
        modifier = Modifier.testTag("lure_config_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // STEP 2 OF 3 Guided Progress Indicator
            TactileStepIndicator(
                currentStep = 2,
                totalSteps = 3,
                stepTitles = listOf("Choose Lure", "Hydrodynamics", "Technical Drawing"),
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            // Live Hydrodynamic Drafting Canvas
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE HYDRODYNAMIC DRAFTING",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ELEVATION + PLAN",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LureOrthographicCanvas(
                        config = currentConfig,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                }
            }

            // Dimensional Specification Sliders
            SynchronizedSliderInput(
                label = "Overall Length",
                value = currentConfig.lengthMm,
                onValueChange = { configViewModel.updateLength(it) },
                min = selectedLure.minLengthMm,
                max = selectedLure.maxLengthMm,
                unit = "mm",
                step = 1f,
                testTagPrefix = "lure_length"
            )

            SynchronizedSliderInput(
                label = "Target Mass (Alloy/Resin)",
                value = currentConfig.weightGrams,
                onValueChange = { configViewModel.updateWeight(it) },
                min = selectedLure.minWeightGrams,
                max = selectedLure.maxWeightGrams,
                unit = "g",
                step = 0.5f,
                testTagPrefix = "lure_weight"
            )

            SynchronizedSliderInput(
                label = "Maximum Body Width",
                value = currentConfig.widthMm,
                onValueChange = { configViewModel.updateWidth(it) },
                min = selectedLure.minWidthMm,
                max = selectedLure.maxWidthMm,
                unit = "mm",
                step = 1f,
                testTagPrefix = "lure_width"
            )

            SynchronizedSliderInput(
                label = "Target Diving Lip Depth",
                value = currentConfig.divingDepthMeters,
                onValueChange = { configViewModel.updateLureParameters(divingDepth = it) },
                min = selectedLure.minDivingDepthMeters,
                max = selectedLure.maxDivingDepthMeters,
                unit = "m",
                step = 0.1f,
                testTagPrefix = "lure_depth"
            )

            // Hydrodynamics & Hardware Parameters
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HYDRODYNAMICS & HARDWARE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buoyancy Action Selector
                    Text(
                        text = "Buoyancy Dynamic",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val buoyancies = listOf("Floating", "Suspending", "Slow Sinking", "Deep Diver")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        buoyancies.forEach { b ->
                            val isSelected = currentConfig.buoyancy == b
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateLureParameters(buoyancy = b) },
                                label = { Text(b) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hook Assembly
                    Text(
                        text = "Terminal Hook Assembly",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val hookTypes = listOf(
                        "#6 BKK Heavy Treble",
                        "#4 BKK Heavy Treble",
                        "#2 Mustad UltraPoint",
                        "Single Assist Hook Set"
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        hookTypes.forEach { hook ->
                            val isSelected = currentConfig.hookType == hook
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateLureParameters(hookType = hook) },
                                label = { Text(hook) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Material Selection
                    Text(
                        text = "Body Material Composition",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedLure.materials.forEach { mat ->
                            val isSelected = currentConfig.material == mat
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateMaterial(mat) },
                                label = { Text(mat) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Realistic Surface Coating & Appearance
            RealisticFinishSelector(
                selectedColorName = currentConfig.colorName,
                onSelectFinish = { finish ->
                    configViewModel.updateColor(
                        name = finish.name.substringBefore(" /"),
                        baseHex = (finish.baseColor.value shr 32).toLong(),
                        accentHex = (finish.accentColor.value shr 32).toLong()
                    )
                }
            )

            // Material & Finish Preview Card
            MaterialAndFinishPreviewCard(
                material = currentConfig.material,
                finishName = currentConfig.colorName,
                baseColor = Color(currentConfig.baseColorHex),
                accentColor = Color(currentConfig.accentColorHex),
                pattern = currentConfig.patternType
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
