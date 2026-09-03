@file:OptIn(ExperimentalLayoutApi::class)

package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.*
import com.example.ui.components.*
import com.example.viewmodel.ConfiguratorViewModel

enum class LureConfigStep(val title: String, val subtitle: String) {
    TYPE("Type", "Lure Action & Profile"),
    SIZE("Size", "Length, Weight & Depth"),
    MATERIAL("Material", "Core Body Alloy/Resin"),
    APPEARANCE("Appearance", "Color, Finish & Pattern"),
    HARDWARE("Hardware", "Hooks, Split Rings & Bib"),
    FEATURES("Features", "Buoyancy, Rattle & Glow"),
    REVIEW("Review", "Production Summary")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LureConfigScreen(
    configViewModel: ConfiguratorViewModel,
    onNavigateToEngineering: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentConfig by configViewModel.currentConfig.collectAsState()
    val selectedLure by configViewModel.selectedLureProduct.collectAsState()

    var currentStep by remember { mutableStateOf(LureConfigStep.TYPE) }
    var showAdvancedProductionSpecs by remember { mutableStateOf(false) }

    val validationIssue = remember(currentConfig) {
        SpecValidation.validateLure(
            lengthMm = currentConfig.lengthMm,
            weightGrams = currentConfig.weightGrams,
            hookQuantity = currentConfig.hookQuantity
        )
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Lure Configurator",
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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep != LureConfigStep.TYPE) {
                        OutlinedButton(
                            onClick = {
                                val steps = LureConfigStep.values()
                                val prevIdx = (currentStep.ordinal - 1).coerceAtLeast(0)
                                currentStep = steps[prevIdx]
                            },
                            modifier = Modifier.testTag("lure_prev_step_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back")
                        }
                    } else {
                        Column {
                            Text(
                                text = "${currentConfig.shape} • ${currentConfig.weightGrams.toInt()}g • ${currentConfig.buoyancy}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Model: ${currentConfig.modelNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (currentStep != LureConfigStep.REVIEW) {
                        Button(
                            onClick = {
                                val steps = LureConfigStep.values()
                                val nextIdx = (currentStep.ordinal + 1).coerceAtMost(steps.size - 1)
                                currentStep = steps[nextIdx]
                            },
                            modifier = Modifier.testTag("lure_next_step_button")
                        ) {
                            Text("Next: ${LureConfigStep.values()[currentStep.ordinal + 1].title}")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        TactileButton(
                            onClick = onNavigateToEngineering,
                            variant = TactileButtonVariant.PRIMARY,
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            text = "Technical Drawing",
                            testTag = "lure_continue_to_engineering_button"
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 1. PRODUCT MODEL HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("7H", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = currentConfig.productName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentConfig.modelNumber} • Step ${currentStep.ordinal + 1} of ${LureConfigStep.values().size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = currentConfig.drawingStatus,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // 2. REALISTIC LIVE PREVIEW (Always visible at top)
            RealisticProductPreviewCanvas(config = currentConfig, heightDp = 180)

            // 3. STEPPER PROGRESS TABS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LureConfigStep.values().forEach { step ->
                    val isSelected = step == currentStep
                    val isPast = step.ordinal < currentStep.ordinal
                    Surface(
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isPast -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { currentStep = step }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if (isPast) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "${step.ordinal + 1}. ${step.title}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    isSelected -> Color.White
                                    isPast -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }

            // 4. ACTIVE STEP CONTENT
            AnimatedContent(
                targetState = currentStep,
                label = "lureStepContentAnimation"
            ) { step ->
                when (step) {
                    LureConfigStep.TYPE -> LureTypeStepContent(currentConfig, configViewModel)
                    LureConfigStep.SIZE -> LureSizeStepContent(currentConfig, selectedLure, configViewModel)
                    LureConfigStep.MATERIAL -> LureMaterialStepContent(currentConfig, configViewModel)
                    LureConfigStep.APPEARANCE -> LureAppearanceStepContent(currentConfig, configViewModel)
                    LureConfigStep.HARDWARE -> LureHardwareStepContent(currentConfig, configViewModel)
                    LureConfigStep.FEATURES -> LureFeaturesStepContent(
                        currentConfig = currentConfig,
                        configViewModel = configViewModel,
                        showAdvanced = showAdvancedProductionSpecs,
                        onToggleAdvanced = { showAdvancedProductionSpecs = it }
                    )
                    LureConfigStep.REVIEW -> LureReviewStepContent(
                        currentConfig = currentConfig,
                        validationIssue = validationIssue,
                        onEdit = { currentStep = LureConfigStep.TYPE },
                        onProceedToDrawing = onNavigateToEngineering
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// -------------------------------------------------------------
// LURE STEP 1: TYPE
// -------------------------------------------------------------
@Composable
private fun LureTypeStepContent(
    config: ProductConfiguration,
    viewModel: ConfiguratorViewModel
) {
    TactileCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "LURE TYPE / ACTION PROFILE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Select the hydrodynamic swimming body type.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LureType.values().forEach { lt ->
                val isSelected = config.shape.equals(lt.displayName, ignoreCase = true)
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            viewModel.updateShape(lt.displayName)
                            viewModel.updateLureParameters(divingDepth = lt.defaultDepthMeters)
                        }
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(lt.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        text = "${lt.defaultDepthMeters}m Depth",
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(lt.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// LURE STEP 2: SIZE, WEIGHT & DEPTH
// -------------------------------------------------------------
@Composable
private fun LureSizeStepContent(
    config: ProductConfiguration,
    selectedLure: LureProduct,
    viewModel: ConfiguratorViewModel
) {
    val quickLureWeights = listOf(15f, 25f, 35f, 45f, 60f, 80f, 100f, 120f)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "TARGET WEIGHT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickLureWeights.forEach { w ->
                        val isSelected = (config.weightGrams - w).let { it in -0.5f..0.5f }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateWeight(w) },
                            label = { Text("${w.toInt()}g") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                SynchronizedSliderInput(
                    label = "Precision Weight Slider",
                    value = config.weightGrams,
                    onValueChange = { viewModel.updateWeight(it) },
                    min = selectedLure.minWeightGrams,
                    max = selectedLure.maxWeightGrams,
                    unit = "g",
                    step = 1f,
                    testTagPrefix = "lure_weight"
                )
            }
        }

        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DIMENSIONS & DIVING DEPTH",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                SynchronizedSliderInput(
                    label = "Body Length",
                    value = config.lengthMm,
                    onValueChange = { viewModel.updateLength(it) },
                    min = selectedLure.minLengthMm,
                    max = selectedLure.maxLengthMm,
                    unit = "mm",
                    step = 2f,
                    testTagPrefix = "lure_length"
                )

                SynchronizedSliderInput(
                    label = "Body Width",
                    value = config.widthMm,
                    onValueChange = { viewModel.updateWidth(it) },
                    min = selectedLure.minWidthMm,
                    max = selectedLure.maxWidthMm,
                    unit = "mm",
                    step = 1f,
                    testTagPrefix = "lure_width"
                )

                SynchronizedSliderInput(
                    label = "Diving Depth Range",
                    value = config.divingDepthMeters,
                    onValueChange = { viewModel.updateLureParameters(divingDepth = it) },
                    min = 0f,
                    max = 8f,
                    unit = "m",
                    step = 0.2f,
                    testTagPrefix = "lure_depth"
                )
            }
        }
    }
}

// -------------------------------------------------------------
// LURE STEP 3: MATERIAL
// -------------------------------------------------------------
@Composable
private fun LureMaterialStepContent(
    config: ProductConfiguration,
    viewModel: ConfiguratorViewModel
) {
    TactileCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "BODY CORE MATERIAL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            LureMaterials.options.forEach { mat ->
                val isSelected = config.material.contains(mat.name.substringBefore(" "), ignoreCase = true)
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.updateMaterial(mat.name) }
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(mat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (mat.isRecommended) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                                        Text("Recommended", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }
                            Text(mat.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// LURE STEP 4: APPEARANCE
// -------------------------------------------------------------
@Composable
private fun LureAppearanceStepContent(
    config: ProductConfiguration,
    viewModel: ConfiguratorViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PRIMARY LURE COLOR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioColors.primaryColors.forEach { c ->
                        val isSelected = (config.baseColorHex and 0x00FFFFFF) == (c.hex and 0x00FFFFFF)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                viewModel.updateColor(c.name, c.hex, config.accentColorHex, config.hasAccentColor)
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(c.color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = if (c.hex == 0xFFF8FAFC) Color.Black else Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(c.name.substringBefore(" "), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ACCENT COLOR",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Switch(
                        checked = config.hasAccentColor,
                        onCheckedChange = {
                            viewModel.updateColor(config.colorName, config.baseColorHex, config.accentColorHex, it)
                        }
                    )
                }

                if (config.hasAccentColor) {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StudioColors.primaryColors.forEach { c ->
                            val isSelected = (config.accentColorHex and 0x00FFFFFF) == (c.hex and 0x00FFFFFF)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(c.color)
                                    .border(if (isSelected) 2.5.dp else 1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable {
                                        viewModel.updateColor(config.colorName, config.baseColorHex, c.hex, true)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = if (c.hex == 0xFFF8FAFC) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LURE FINISH & PATTERN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StudioFinish.values().forEach { fin ->
                        val isSelected = config.finishType.equals(fin.displayName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateFinish(fin.displayName) },
                            label = { Text(fin.displayName) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text("Surface Pattern", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StudioPattern.values().forEach { pat ->
                        val isSelected = config.patternName.equals(pat.displayName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updatePattern(pat.displayName, JigPatternType.SOLID_STRIPE) },
                            label = { Text(pat.displayName) }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// LURE STEP 5: HARDWARE
// -------------------------------------------------------------
@Composable
private fun LureHardwareStepContent(
    config: ProductConfiguration,
    viewModel: ConfiguratorViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "HOOK & RIGGING SPECIFICATIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text("Treble Hook Quantity", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3).forEach { qty ->
                        val isSelected = config.hookQuantity == qty
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateHook(config.hookLevel, config.hookStyle, config.hookSize, qty) },
                            label = { Text("$qty Treble Hooks") }
                        )
                    }
                }

                val hookQtyIssue = remember(config.lengthMm, config.hookQuantity) {
                    SpecValidation.validateLure(config.lengthMm, config.weightGrams, config.hookQuantity)
                }
                if (hookQtyIssue != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(hookQtyIssue.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                hookQtyIssue.recommendedValue?.let {
                                    Text(it, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                }

                Text("Hook Size", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("#6", "#4", "#2", "#1", "1/0", "2/0", "3/0").forEach { sz ->
                        val isSelected = config.hookSize == sz
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateHook(config.hookLevel, config.hookStyle, sz, config.hookQuantity) },
                            label = { Text(sz) }
                        )
                    }
                }

                Text("Hardware Grade", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("VMC Saltwater 3X Treble", "Owner ST-66 4X Heavy", "Mustad UltraPoint").forEach { hType ->
                        val isSelected = config.hookType == hType
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateLureParameters(hookType = hType) },
                            label = { Text(hType, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// LURE STEP 6: SPECIAL FEATURES & BUOYANCY
// -------------------------------------------------------------
@Composable
private fun LureFeaturesStepContent(
    currentConfig: ProductConfiguration,
    configViewModel: ConfiguratorViewModel,
    showAdvanced: Boolean,
    onToggleAdvanced: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "BUOYANCY DYNAMICS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Floating", "Suspending", "Sinking").forEach { b ->
                        val isSelected = currentConfig.buoyancy.equals(b, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { configViewModel.updateLureParameters(buoyancy = b) },
                            label = { Text(b, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "ACOUSTICS & LIGHT ATTRACTION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FeatureToggleRow(
                    title = "Internal Acoustic Tungsten Rattle",
                    subtitle = "High-frequency sonic pitch across retrieval",
                    checked = currentConfig.hasRattle,
                    onCheckedChange = {
                        configViewModel.updateSpecialFeatures(currentConfig.hasGlow, currentConfig.hasUvReactive, it, currentConfig.hasWeedGuard, currentConfig.customMarking)
                    }
                )

                FeatureToggleRow(
                    title = "Phosphorescent Glow Coating",
                    subtitle = "Luminous charging under sunlight for deep/night strikes",
                    checked = currentConfig.hasGlow,
                    onCheckedChange = {
                        configViewModel.updateSpecialFeatures(it, currentConfig.hasUvReactive, currentConfig.hasRattle, currentConfig.hasWeedGuard, currentConfig.customMarking)
                    }
                )

                FeatureToggleRow(
                    title = "UV Reactive Finish",
                    subtitle = "Vibrant underwater edge illumination",
                    checked = currentConfig.hasUvReactive,
                    onCheckedChange = {
                        configViewModel.updateSpecialFeatures(currentConfig.hasGlow, it, currentConfig.hasRattle, currentConfig.hasWeedGuard, currentConfig.customMarking)
                    }
                )
            }
        }

        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ADVANCED PRODUCTION SPECIFICATIONS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Drawing revisions & manufacturing notes",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = showAdvanced, onCheckedChange = onToggleAdvanced)
                }

                if (showAdvanced) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text("Drawing Revision", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("A", "B", "C").forEach { rev ->
                            FilterChip(
                                selected = currentConfig.revision == rev,
                                onClick = { configViewModel.updateRevision(rev) },
                                label = { Text("Revision $rev") }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = currentConfig.generalTolerance,
                            onValueChange = { configViewModel.updateTolerances(it, currentConfig.weightTolerance) },
                            label = { Text("General Tolerance") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = currentConfig.weightTolerance,
                            onValueChange = { configViewModel.updateTolerances(currentConfig.generalTolerance, it) },
                            label = { Text("Weight Tolerance") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// LURE STEP 7: REVIEW
// -------------------------------------------------------------
@Composable
private fun LureReviewStepContent(
    currentConfig: ProductConfiguration,
    validationIssue: SpecValidation.ValidationIssue?,
    onEdit: () -> Unit,
    onProceedToDrawing: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (validationIssue != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "This combination isn't available for this product.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        validationIssue.recommendedValue?.let {
                            Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LURE CONFIGURATION SUMMARY",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = currentConfig.productName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Drawing: ${currentConfig.effectiveDrawingNumber}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                val summaryItems = listOf(
                    "Model" to currentConfig.modelNumber,
                    "Lure Type" to currentConfig.shape,
                    "Target Weight" to "${currentConfig.weightGrams.toInt()} g",
                    "Dimensions" to "${currentConfig.lengthMm.toInt()}L x ${currentConfig.widthMm.toInt()}W mm",
                    "Diving Depth" to "${String.format(java.util.Locale.US, "%.1f", currentConfig.divingDepthMeters)} m",
                    "Buoyancy" to currentConfig.buoyancy,
                    "Body Core" to currentConfig.material,
                    "Color Theme" to "${currentConfig.colorName}${if (currentConfig.hasAccentColor) " (with Accent)" else ""}",
                    "Finish & Pattern" to "${currentConfig.finishType} • ${currentConfig.patternName}",
                    "Hook Rigging" to "${currentConfig.hookQuantity}x ${currentConfig.hookSize} (${currentConfig.hookType})",
                    "Acoustics & Light" to listOfNotNull(
                        if (currentConfig.hasRattle) "Rattle" else null,
                        if (currentConfig.hasGlow) "Glow" else null,
                        if (currentConfig.hasUvReactive) "UV" else null
                    ).ifEmpty { listOf("None") }.joinToString(", ")
                )

                summaryItems.forEach { (k, v) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(k, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(v, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    Button(onClick = onProceedToDrawing, modifier = Modifier.weight(1.5f)) {
                        Icon(Icons.Default.DesignServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Technical Drawing")
                    }
                }
            }
        }

        // CAD PREVIEW
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "CAD SCHEMATIC PREVIEW",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LureEngineeringCanvas(config = currentConfig)
            }
        }
    }
}
