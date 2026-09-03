package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.viewmodel.ConfiguratorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RodConfigScreen(
    configViewModel: ConfiguratorViewModel,
    onNavigateToEngineering: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentConfig by configViewModel.currentConfig.collectAsState()
    val selectedRod by configViewModel.selectedRodProduct.collectAsState()

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
                            text = "${String.format(java.util.Locale.US, "%.2f", currentConfig.lengthMm / 1000f)}m (${currentConfig.lengthMm.toInt()}mm)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${currentConfig.power} • ${currentConfig.action} Action",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    TactileButton(
                        onClick = onNavigateToEngineering,
                        variant = TactileButtonVariant.SECONDARY,
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        text = "Technical Drawing",
                        testTag = "rod_continue_to_dashboard_button"
                    )
                }
            }
        },
        modifier = Modifier.testTag("rod_config_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // STEP 2 OF 3 Guided Progress Indicator
            TactileStepIndicator(
                currentStep = 2,
                totalSteps = 3,
                stepTitles = listOf("Choose Rod", "Technical Specs", "Technical Drawing"),
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            // PRODUCT SPEC HEADER
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ENGINEERING DISCIPLINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "${currentConfig.rodType} Rod Architecture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Model Series: ${currentConfig.modelNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "AEROSPACE",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // DIMENSIONAL PARAMETERS (Tactile Synchronized Sliders)
            Text(
                text = "DIMENSIONS & LOADING CAPACITIES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            // 1. Overall Length (mm)
            SynchronizedSliderInput(
                label = "Overall Blank Length",
                value = currentConfig.lengthMm,
                onValueChange = { configViewModel.updateLength(it) },
                min = selectedRod.minLengthMm,
                max = selectedRod.maxLengthMm,
                unit = "mm",
                step = 50f,
                testTagPrefix = "rod_length"
            )

            // 2. Rod Finished Weight (g)
            SynchronizedSliderInput(
                label = "Finished Blank Weight",
                value = currentConfig.weightGrams,
                onValueChange = { configViewModel.updateWeight(it) },
                min = selectedRod.minWeightGrams,
                max = selectedRod.maxWeightGrams,
                unit = "g",
                step = 5f,
                testTagPrefix = "rod_weight"
            )

            // 3. Butt Diameter (mm)
            SynchronizedSliderInput(
                label = "Butt Outer Blank Diameter",
                value = currentConfig.widthMm,
                onValueChange = { configViewModel.updateWidth(it) },
                min = 7f,
                max = 22f,
                unit = "mm",
                step = 0.5f,
                testTagPrefix = "rod_diameter"
            )

            // 4. Maximum Deadlift Load (kg)
            SynchronizedSliderInput(
                label = "Structural Max Deadlift Load",
                value = currentConfig.maximumLoadKg,
                onValueChange = { configViewModel.updateRodParameters(maxLoad = it) },
                min = 4f,
                max = 40f,
                unit = "kg",
                step = 1f,
                testTagPrefix = "rod_max_load"
            )

            // 5. Handle Length (mm)
            SynchronizedSliderInput(
                label = "Rear Grip / Handle Length",
                value = currentConfig.handleLengthMm,
                onValueChange = { configViewModel.updateRodParameters(handleLength = it) },
                min = 280f,
                max = 600f,
                unit = "mm",
                step = 10f,
                testTagPrefix = "rod_handle_len"
            )

            // Power Rating Selection
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ROD POWER RATING",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedRod.powers.forEach { power ->
                            val isSelected = currentConfig.power == power
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateRodParameters(power = power) },
                                label = { Text(power, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Action Selection
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TAPER / DEFLECTION ACTION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedRod.actions.forEach { action ->
                            val isSelected = currentConfig.action == action
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateRodParameters(action = action) },
                                label = { Text(action, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Section Count & Material Construction
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PIECE COUNT & CARBON CONSTRUCTION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Sections / Pieces:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedRod.sectionOptions.forEach { count ->
                            val isSelected = currentConfig.sections == count
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateRodParameters(sections = count) },
                                label = { Text("$count Piece${if (count > 1) "s" else ""}", fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Carbon Blank Material Layup:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedRod.materials.forEach { mat ->
                            val isSelected = currentConfig.material == mat
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateMaterial(mat) },
                                label = { Text(mat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }
            }

            // LIVE TECHNICAL DRAWING SCHEMATIC PREVIEW
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TECHNICAL DRAWING SCHEMATIC",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RodEngineeringCanvas(config = currentConfig)
                }
            }

            // ILLUSTRATIVE FINITE ELEMENT LOAD SIMULATION (At bottom, per Section 13 & 22)
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "FINITE ELEMENT DEFLECTION & LOAD SIMULATION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RodBreakageAnimation(config = currentConfig)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
