package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhonelinkRing
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
import com.example.data.model.JigPatternType
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

    var isMoreOptionsExpanded by remember { mutableStateOf(false) }

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
                            text = "Model: ${currentConfig.modelNumber} • FR: ${currentConfig.frontRing} • BR: ${currentConfig.backRing}",
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isWideScreen = maxWidth >= 720.dp

            if (isWideScreen) {
                // TABLET / DESKTOP TWO-COLUMN RESPONSIVE LAYOUT (Requirement 3)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // LEFT COLUMN: Product Preview, Technical View, Product in Action
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Product Name Header
                        ProductHeaderCard(config = currentConfig)

                        // HERO PRODUCT PREVIEW
                        JigProductPreview(
                            config = currentConfig,
                            selectedJig = selectedJig
                        )

                        // TECHNICAL VIEW (Requirement 14 & 15)
                        TechnicalViewSection(config = currentConfig)

                        // PRODUCT IN ACTION (Requirement 16 & 17)
                        ProductInActionSection(config = currentConfig)
                    }

                    // RIGHT COLUMN: Configuration Controls
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Guided Step Indicator
                        TactileStepIndicator(
                            currentStep = 2,
                            totalSteps = 3,
                            stepTitles = listOf("Choose Jig", "Configure Specs", "CAD Drawing")
                        )

                        // Essential Configuration Controls
                        EssentialConfigurationControls(
                            config = currentConfig,
                            selectedJig = selectedJig,
                            configViewModel = configViewModel
                        )

                        // More Options Progressive Disclosure
                        MoreOptionsSection(
                            config = currentConfig,
                            selectedJig = selectedJig,
                            configViewModel = configViewModel,
                            isExpanded = isMoreOptionsExpanded,
                            onToggleExpand = { isMoreOptionsExpanded = !isMoreOptionsExpanded }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                // MOBILE COMPACT VERTICAL LAYOUT (Requirement 3)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // Step Indicator
                    TactileStepIndicator(
                        currentStep = 2,
                        totalSteps = 3,
                        stepTitles = listOf("Choose Jig", "Configure Specs", "CAD Drawing")
                    )

                    // 1. PRODUCT HEADER
                    ProductHeaderCard(config = currentConfig)

                    // 2. PRODUCT PREVIEW (VISUAL HERO)
                    JigProductPreview(
                        config = currentConfig,
                        selectedJig = selectedJig
                    )

                    // 3. ESSENTIAL CONFIGURATION
                    EssentialConfigurationControls(
                        config = currentConfig,
                        selectedJig = selectedJig,
                        configViewModel = configViewModel
                    )

                    // 4. MORE OPTIONS (Progressive Disclosure Accordion)
                    MoreOptionsSection(
                        config = currentConfig,
                        selectedJig = selectedJig,
                        configViewModel = configViewModel,
                        isExpanded = isMoreOptionsExpanded,
                        onToggleExpand = { isMoreOptionsExpanded = !isMoreOptionsExpanded }
                    )

                    // 5. TECHNICAL VIEW (Requirement 14 & 15)
                    TechnicalViewSection(config = currentConfig)

                    // 6. PRODUCT IN ACTION (Requirement 16 & 17)
                    ProductInActionSection(config = currentConfig)

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Product Specification Header
 */
@Composable
private fun ProductHeaderCard(config: com.example.data.model.ProductConfiguration) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp
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
                    text = "COMMERCIAL PRODUCT CONFIGURATOR",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = config.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Model: ${config.modelNumber} • Category: ${config.category}",
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
                    text = "OEM SPEC",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Essential Configuration Section:
 * Dimensions (Weight, Length, Width), Finish & Colors, Front & Back Rings, Hook, Thread
 */
@Composable
private fun EssentialConfigurationControls(
    config: com.example.data.model.ProductConfiguration,
    selectedJig: com.example.data.model.JigProduct,
    configViewModel: ConfiguratorViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // DIMENSIONAL PARAMETERS
        Text(
            text = "DIMENSIONAL PARAMETERS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        // Weight Input
        SynchronizedSliderInput(
            label = "Target Finished Mass",
            value = config.weightGrams,
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
            value = config.lengthMm,
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
            value = config.widthMm,
            onValueChange = { configViewModel.updateWidth(it) },
            min = selectedJig.minWidthMm,
            max = selectedJig.maxWidthMm,
            unit = "mm",
            step = 1f,
            testTagPrefix = "jig_width"
        )

        // FINISH & COLOR SELECTION
        RealisticFinishSelector(
            selectedColorName = config.colorName,
            onSelectFinish = { finish ->
                configViewModel.updateFinish(
                    finishName = finish.name.substringBefore(" /"),
                    baseHex = (finish.baseColor.value shr 32).toLong(),
                    accentHex = (finish.accentColor.value shr 32).toLong(),
                    patternType = when {
                        finish.name.contains("Holographic", ignoreCase = true) -> JigPatternType.HOLOGRAPHIC_SLASH
                        finish.name.contains("Candy", ignoreCase = true) -> JigPatternType.DOT_PATTERN
                        else -> JigPatternType.SOLID_STRIPE
                    }
                )
            }
        )

        // RINGS CONFIGURATION (Requirement 8, 9, 10)
        RingsConfigCard(
            config = config,
            selectedJig = selectedJig,
            onUpdateFrontRing = { configViewModel.updateFrontRing(it) },
            onUpdateBackRing = { configViewModel.updateBackRing(it) }
        )

        // HOOK RIGGING CONFIGURATION
        HookRiggingCard(
            config = config,
            selectedJig = selectedJig,
            onSelectHook = { configViewModel.updateHook(it) }
        )

        // THREAD CONFIGURATION
        ThreadSelector(
            selectedThread = config.threadColor,
            onSelectThread = { threadOption ->
                configViewModel.updateThread(
                    threadColor = threadOption.name,
                    colorHex = threadOption.color?.let { (it.value shr 32).toLong() }
                )
            }
        )
    }
}

/**
 * Rings Configuration Card (Requirement 8, 9, 10)
 * Allows configuring Front Ring and Back Ring (None, Standard, Heavy Duty, Custom)
 */
@Composable
private fun RingsConfigCard(
    config: com.example.data.model.ProductConfiguration,
    selectedJig: com.example.data.model.JigProduct,
    onUpdateFrontRing: (String) -> Unit,
    onUpdateBackRing: (String) -> Unit
) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ATTACHMENT RINGS (FRONT & BACK)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "SUS304 STAINLESS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FRONT RING
            Text(
                text = "Front Ring (Line Tie / Assist Attachment):",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("None", "Standard", "Heavy Duty", "Custom").forEach { ringOption ->
                    val isSelected = config.frontRing == ringOption
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateFrontRing(ringOption) },
                        label = { Text(ringOption, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // BACK RING
            Text(
                text = "Back Ring (Rear Stinger / Tail Attachment):",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("None", "Standard", "Heavy Duty", "Custom").forEach { ringOption ->
                    val isSelected = config.backRing == ringOption
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateBackRing(ringOption) },
                        label = { Text(ringOption, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Hook Rigging Configuration Card
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HookRiggingCard(
    config: com.example.data.model.ProductConfiguration,
    selectedJig: com.example.data.model.JigProduct,
    onSelectHook: (String) -> Unit
) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "HOOK RIGGING CONFIGURATION",
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
                selectedJig.availableHooks.forEach { hook ->
                    val isSelected = config.hookTypeJig == hook
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectHook(hook) },
                        label = { Text(hook, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}

/**
 * More Options Section (Accordion Progressive Disclosure)
 * Core Alloy, 3D Eye Style, Assist Cord Type, Tolerance & Quality Notes
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoreOptionsSection(
    config: com.example.data.model.ProductConfiguration,
    selectedJig: com.example.data.model.JigProduct,
    configViewModel: ConfiguratorViewModel,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "MORE OPTIONS (ALLOY, EYE & CORD)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. ALLOY & CORE CONSTRUCTION
                    Text(
                        text = "Core Alloy Construction:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedJig.materials.forEach { mat ->
                            val isSelected = config.material == mat
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateMaterial(mat) },
                                label = { Text(mat, fontSize = 11.5.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }

                    // 2. 3D EYE STYLE
                    Text(
                        text = "3D Strike Eye Specification:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "3D Luminous Strike Eye",
                            "Holographic Foil Eye",
                            "High-Contrast Target Eye",
                            "Custom Etched Eye"
                        ).forEach { eye ->
                            val isSelected = config.eyeStyle == eye
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateEyeStyle(eye) },
                                label = { Text(eye, fontSize = 11.5.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }

                    // 3. ASSIST CORD MATERIAL
                    Text(
                        text = "Assist Cord Tensile Rigging:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Braided PE (200 lb)",
                            "Kevlar Core (250 lb)",
                            "Wire Assist (150 lb)"
                        ).forEach { cord ->
                            val isSelected = config.assistCord == cord
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateAssistCord(cord) },
                                label = { Text(cord, fontSize = 11.5.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }

                    // 4. MANUFACTURING TOLERANCE NOTE
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MANUFACTURING TOLERANCE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "TBD / ISO 2768-m",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
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
 * Technical View Section (Requirement 14 & 15)
 * Dedicated engineering 5-view orthographic canvas with rings and dimension lines
 */
@Composable
private fun TechnicalViewSection(config: com.example.data.model.ProductConfiguration) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TECHNICAL VIEW",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "ORTHOGRAPHIC PROJECTION (1:1)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            JigEngineeringCanvas(config = config)
        }
    }
}

/**
 * Product in Action Section (Requirement 16 & 17)
 * High-quality rendered/composited presentation, clearly labeled as visual presentation (non-simulation)
 */
@Composable
private fun ProductInActionSection(config: com.example.data.model.ProductConfiguration) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRODUCT IN ACTION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Surface(
                    color = Color(0xFF0F2942),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "VISUAL PRESENTATION",
                        color = Color(0xFF38BDF8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            JigFishingAnimation(config = config)
        }
    }
}
