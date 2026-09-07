package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
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
fun JigConfigScreen(
    configViewModel: ConfiguratorViewModel,
    onNavigateToEngineering: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentConfig by configViewModel.currentConfig.collectAsState()
    val selectedJig by configViewModel.selectedJigProduct.collectAsState()

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
                            text = "${currentConfig.lengthMm.toInt()}mm • ${currentConfig.weightGrams.toInt()}g",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Model: ${currentConfig.modelNumber}",
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
                        testTag = "jig_continue_to_dashboard_button"
                    )
                }
            }
        },
        modifier = Modifier.testTag("jig_config_screen")
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
                stepTitles = listOf("Choose Jig", "Technical Specs", "Technical Drawing"),
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            // SECTION 1: PRODUCT HEADER & LIVE READOUT
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
                            text = "PRODUCT SPECIFICATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = currentConfig.productName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Model: ${currentConfig.modelNumber} • Category: ${currentConfig.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ISO 9001",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // SECTION 2: DIMENSIONAL PARAMETERS (Tactile Synchronized Sliders)
            Text(
                text = "DIMENSIONAL PARAMETERS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            // Weight Input
            SynchronizedSliderInput(
                label = "Lure Finished Mass",
                value = currentConfig.weightGrams,
                onValueChange = { configViewModel.updateWeight(it) },
                min = selectedJig.minWeightGrams,
                max = selectedJig.maxWeightGrams,
                unit = "g",
                step = 5f,
                testTagPrefix = "jig_weight"
            )

            // Length Input
            SynchronizedSliderInput(
                label = "Total Overall Length",
                value = currentConfig.lengthMm,
                onValueChange = { configViewModel.updateLength(it) },
                min = selectedJig.minLengthMm,
                max = selectedJig.maxLengthMm,
                unit = "mm",
                step = 5f,
                testTagPrefix = "jig_length"
            )

            // Width Input
            SynchronizedSliderInput(
                label = "Maximum Hydro Body Width",
                value = currentConfig.widthMm,
                onValueChange = { configViewModel.updateWidth(it) },
                min = selectedJig.minWidthMm,
                max = selectedJig.maxWidthMm,
                unit = "mm",
                step = 1f,
                testTagPrefix = "jig_width"
            )

            // SECTION 3: ALLOY & CORE CONSTRUCTION
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ALLOY & CORE CONSTRUCTION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedJig.materials.forEach { mat ->
                            val isSelected = currentConfig.material == mat
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateMaterial(mat) },
                                label = { Text(mat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // SECTION 4: REALISTIC MATERIAL & FINISH SELECTION
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

            // Material & Finish Preview Spec Card
            MaterialAndFinishPreviewCard(
                material = currentConfig.material,
                finishName = currentConfig.colorName,
                baseColor = Color(currentConfig.baseColorHex),
                accentColor = Color(currentConfig.accentColorHex),
                pattern = currentConfig.patternType
            )

            // SECTION 5: THREAD CONFIGURATION (Sections 10, 11, 12)
            ThreadSelector(
                selectedThread = currentConfig.threadColor,
                onSelectThread = { threadOption ->
                    configViewModel.updateThread(
                        threadColor = threadOption.name,
                        colorHex = threadOption.color?.let { (it.value shr 32).toLong() }
                    )
                }
            )

            // SECTION 6: LIVE TECHNICAL DRAWING SCHEMATIC PREVIEW
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TECHNICAL DRAWING SCHEMATIC",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    JigEngineeringCanvas(config = currentConfig)
                }
            }

            // SECTION 7: PRODUCT PRESENTATION — ACTION & RIGGING (Section 16: Product in Action)
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PRODUCT PRESENTATION — ACTION & RIGGING",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    JigFishingAnimation(config = currentConfig)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
