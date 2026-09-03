@file:OptIn(ExperimentalLayoutApi::class)

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.viewmodel.ConfiguratorViewModel

enum class JigConfigStep(val title: String, val subtitle: String) {
    SHAPE("Shape", "Body & Head Style"),
    SIZE("Size", "Weight & Dimensions"),
    APPEARANCE("Appearance", "Color, Finish & Eye"),
    HARDWARE("Hardware", "Hook & Accessories"),
    FEATURES("Features", "Special & Advanced Specs"),
    REVIEW("Review", "Production Summary")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JigConfigScreen(
    configViewModel: ConfiguratorViewModel,
    onNavigateToEngineering: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentConfig by configViewModel.currentConfig.collectAsState()
    val selectedJig by configViewModel.selectedJigProduct.collectAsState()

    var currentStep by remember { mutableStateOf(JigConfigStep.SHAPE) }
    var showAdvancedProductionSpecs by remember { mutableStateOf(false) }

    // Validation
    val validationIssue = remember(currentConfig) {
        SpecValidation.validateJig(
            weightGrams = currentConfig.weightGrams,
            lengthMm = currentConfig.lengthMm,
            hookSize = currentConfig.hookSize,
            minWeight = selectedJig.minWeightGrams,
            maxWeight = selectedJig.maxWeightGrams
        )
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Jig Configurator",
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
                    // Previous button or Step indicator
                    if (currentStep != JigConfigStep.SHAPE) {
                        OutlinedButton(
                            onClick = {
                                val steps = JigConfigStep.values()
                                val prevIdx = (currentStep.ordinal - 1).coerceAtLeast(0)
                                currentStep = steps[prevIdx]
                            },
                            modifier = Modifier.testTag("jig_prev_step_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back")
                        }
                    } else {
                        Column {
                            Text(
                                text = "${currentConfig.shape} • ${currentConfig.weightGrams.toInt()}g",
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

                    // Next button or Go to Drawing
                    if (currentStep != JigConfigStep.REVIEW) {
                        Button(
                            onClick = {
                                val steps = JigConfigStep.values()
                                val nextIdx = (currentStep.ordinal + 1).coerceAtMost(steps.size - 1)
                                currentStep = steps[nextIdx]
                            },
                            modifier = Modifier.testTag("jig_next_step_button")
                        ) {
                            Text("Next: ${JigConfigStep.values()[currentStep.ordinal + 1].title}")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        TactileButton(
                            onClick = onNavigateToEngineering,
                            variant = TactileButtonVariant.PRIMARY,
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            text = "Technical Drawing",
                            testTag = "jig_continue_to_dashboard_button"
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 1. PRODUCT HEADER CHIP
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
                            text = "${currentConfig.modelNumber} • Step ${currentStep.ordinal + 1} of ${JigConfigStep.values().size}",
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

            // 2. INTERACTIVE REALISTIC JIG PREVIEW (Always visible at top for immediate feedback)
            RealisticProductPreviewCanvas(config = currentConfig, heightDp = 180)

            // 3. PROGRESSIVE STEP NAVIGATION BAR (Horizontal pill tabs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                JigConfigStep.values().forEach { step ->
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
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
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
                label = "stepContentAnimation"
            ) { step ->
                when (step) {
                    JigConfigStep.SHAPE -> ShapeStepContent(currentConfig, configViewModel)
                    JigConfigStep.SIZE -> SizeStepContent(currentConfig, selectedJig, configViewModel)
                    JigConfigStep.APPEARANCE -> AppearanceStepContent(currentConfig, configViewModel)
                    JigConfigStep.HARDWARE -> HardwareStepContent(currentConfig, configViewModel)
                    JigConfigStep.FEATURES -> FeaturesStepContent(
                        currentConfig = currentConfig,
                        configViewModel = configViewModel,
                        showAdvanced = showAdvancedProductionSpecs,
                        onToggleAdvanced = { showAdvancedProductionSpecs = it }
                    )
                    JigConfigStep.REVIEW -> ReviewStepContent(
                        currentConfig = currentConfig,
                        validationIssue = validationIssue,
                        onEdit = { currentStep = JigConfigStep.SHAPE },
                        onProceedToDrawing = onNavigateToEngineering
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// -------------------------------------------------------------
// STEP 1: SHAPE
// -------------------------------------------------------------
@Composable
private fun ShapeStepContent(
    config: ProductConfiguration,
    viewModel: ConfiguratorViewModel
) {
    TactileCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "HEAD / BODY SHAPE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Choose the basic hydrodynamic shape of the jig.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            JigShape.values().forEach { shape ->
                val isSelected = config.shape.equals(shape.displayName, ignoreCase = true)
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.updateShape(shape.displayName) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = shape.displayName.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = shape.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = shape.categoryHint,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = shape.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 2: SIZE & WEIGHT & MATERIAL
// -------------------------------------------------------------
@Composable
private fun SizeStepContent(
    config: ProductConfiguration,
    selectedJig: JigProduct,
    viewModel: ConfiguratorViewModel
) {
    val quickWeights = listOf(40f, 50f, 60f, 70f, 80f, 100f, 120f, 150f, 180f, 200f)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // QUICK-SELECT WEIGHT
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TARGET WEIGHT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${config.weightGrams.toInt()} g",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal quick selection chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickWeights.forEach { w ->
                        val isSelected = (config.weightGrams - w).let { it in -0.5f..0.5f }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateWeight(w) },
                            label = { Text("${w.toInt()}g", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Weight Slider & numeric input
                SynchronizedSliderInput(
                    label = "Custom Weight Precision",
                    value = config.weightGrams,
                    onValueChange = { viewModel.updateWeight(it) },
                    min = selectedJig.minWeightGrams,
                    max = selectedJig.maxWeightGrams,
                    unit = "g",
                    step = 1f,
                    testTagPrefix = "jig_weight"
                )

                val weightIssue = remember(config.weightGrams, selectedJig) {
                    SpecValidation.validateJig(config.weightGrams, config.lengthMm, config.hookSize, selectedJig.minWeightGrams, selectedJig.maxWeightGrams)
                }
                if (weightIssue != null && weightIssue.message.contains("weight", ignoreCase = true)) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(weightIssue.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        // DIMENSIONS
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DIMENSIONAL PROPORTIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Length
                SynchronizedSliderInput(
                    label = "Overall Length",
                    value = config.lengthMm,
                    onValueChange = { viewModel.updateLength(it) },
                    min = selectedJig.minLengthMm,
                    max = selectedJig.maxLengthMm,
                    unit = "mm",
                    step = 2f,
                    testTagPrefix = "jig_length"
                )

                // Width
                SynchronizedSliderInput(
                    label = "Maximum Width",
                    value = config.widthMm,
                    onValueChange = { viewModel.updateWidth(it) },
                    min = selectedJig.minWidthMm,
                    max = selectedJig.maxWidthMm,
                    unit = "mm",
                    step = 1f,
                    testTagPrefix = "jig_width"
                )

                // Height / Thickness
                SynchronizedSliderInput(
                    label = "Body Thickness / Height",
                    value = config.heightMm,
                    onValueChange = { viewModel.updateHeight(it) },
                    min = 6f,
                    max = 30f,
                    unit = "mm",
                    step = 1f,
                    testTagPrefix = "jig_height"
                )
            }
        }

        // BODY MATERIAL
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "BODY MATERIAL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                JigMaterials.options.forEach { mat ->
                    val isSelected = config.material.contains(mat.name, ignoreCase = true)
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
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(mat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (mat.isRecommended) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "Recommended",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
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
}

// -------------------------------------------------------------
// STEP 3: APPEARANCE (Color, Accent, Finish, Pattern, Eye)
// -------------------------------------------------------------
@Composable
private fun AppearanceStepContent(
    config: ProductConfiguration,
    viewModel: ConfiguratorViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // PRIMARY COLOR
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PRIMARY COLOR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioColors.primaryColors.forEach { c ->
                        val isSelected = (config.baseColorHex and 0x00FFFFFF) == (c.hex and 0x00FFFFFF)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                viewModel.updateColor(
                                    name = c.name,
                                    baseHex = c.hex,
                                    accentHex = config.accentColorHex,
                                    hasAccent = config.hasAccentColor
                                )
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
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (c.hex == 0xFFF8FAFC) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(c.name.substringBefore(" "), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // ACCENT COLOR
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACCENT COLOR",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Accent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = config.hasAccentColor,
                            onCheckedChange = {
                                viewModel.updateColor(
                                    name = config.colorName,
                                    baseHex = config.baseColorHex,
                                    accentHex = config.accentColorHex,
                                    hasAccent = it
                                )
                            }
                        )
                    }
                }

                if (config.hasAccentColor) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StudioColors.primaryColors.forEach { c ->
                            val isSelected = (config.accentColorHex and 0x00FFFFFF) == (c.hex and 0x00FFFFFF)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    viewModel.updateColor(
                                        name = "${config.colorName.substringBefore("-")}-${c.name.substringBefore(" ")}",
                                        baseHex = config.baseColorHex,
                                        accentHex = c.hex,
                                        hasAccent = true
                                    )
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(c.color)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (c.hex == 0xFFF8FAFC) Color.Black else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // BODY FINISH
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "BODY FINISH",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Distinguish color from optical finish and reflective properties.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StudioFinish.values().forEach { fin ->
                        val isSelected = config.finishType.equals(fin.displayName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateFinish(fin.displayName) },
                            label = { Text(fin.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // SURFACE PATTERN
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SURFACE PATTERN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StudioPattern.values().forEach { pat ->
                        val isSelected = config.patternName.equals(pat.displayName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val jigPat = when (pat) {
                                    StudioPattern.DOTS -> JigPatternType.DOT_PATTERN
                                    StudioPattern.STRIPES, StudioPattern.TIGER -> JigPatternType.HOLOGRAPHIC_SLASH
                                    StudioPattern.DIAMONDS -> JigPatternType.CRYSTAL_FACET
                                    else -> JigPatternType.SOLID_STRIPE
                                }
                                viewModel.updatePattern(pat.displayName, jigPat)
                            },
                            label = { Text(pat.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }

        // EYE CONFIGURATION
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "EYE CONFIGURATION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Eye Style
                Text("Eye Style", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EyeStyle.values().forEach { style ->
                        val isSelected = config.eyeStyle.equals(style.displayName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateEye(style.displayName, config.eyeShape, config.eyeColorHex) },
                            label = { Text(style.displayName) }
                        )
                    }
                }

                // Eye Shape
                Text("Eye Shape", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EyeShape.values().forEach { shape ->
                        val isSelected = config.eyeShape.equals(shape.displayName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateEye(config.eyeStyle, shape.displayName, config.eyeColorHex) },
                            label = { Text(shape.displayName) }
                        )
                    }
                }

                // Eye Color
                Text("Eye Color", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudioColors.eyeColors.forEach { c ->
                        val isSelected = (config.eyeColorHex and 0x00FFFFFF) == (c.hex and 0x00FFFFFF)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c.color)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8),
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.updateEye(config.eyeStyle, config.eyeShape, c.hex)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STEP 4: HARDWARE (Hook, Hook Size, Additional Components)
// -------------------------------------------------------------
@Composable
private fun HardwareStepContent(
    config: ProductConfiguration,
    viewModel: ConfiguratorViewModel
) {
    var showAdvancedHooks by remember { mutableStateOf(false) }
    val hookSizes = listOf("#4", "#2", "#1", "1/0", "2/0", "3/0", "4/0", "5/0")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // HOOK SELECTION
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "HOOK CONFIGURATION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Simple hook level
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HookLevel.values().forEach { lvl ->
                        val isSelected = config.hookLevel.equals(lvl.displayName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateHook(lvl.displayName, config.hookStyle, config.hookSize, config.hookQuantity)
                            },
                            label = { Text(lvl.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                // Hook Size
                Text("Hook Size", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hookSizes.forEach { sz ->
                        val isSelected = config.hookSize == sz
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateHook(config.hookLevel, config.hookStyle, sz, config.hookQuantity)
                            },
                            label = { Text(sz, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                val hookIssue = remember(config.lengthMm, config.hookSize) {
                    SpecValidation.validateJig(config.weightGrams, config.lengthMm, config.hookSize, 0f, 9999f)
                }
                if (hookIssue != null && hookIssue.message.contains("Hook", ignoreCase = true)) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(hookIssue.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                hookIssue.recommendedValue?.let {
                                    Text(it, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                }

                // Advanced Hook Toggle
                TextButton(
                    onClick = { showAdvancedHooks = !showAdvancedHooks },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (showAdvancedHooks) "Hide Advanced Hook Specs" else "Advanced Hook Specifications")
                    Icon(
                        imageVector = if (showAdvancedHooks) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }

                if (showAdvancedHooks) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Hook Geometry Family", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            HookFamily.values().forEach { fam ->
                                val isSelected = config.hookStyle.equals(fam.displayName, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updateHook(config.hookLevel, fam.displayName, config.hookSize, config.hookQuantity)
                                    },
                                    label = { Text(fam.displayName, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ADDITIONAL BODY COMPONENT (Skirt / Trailer / Soft Components)
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ADDITIONAL BODY COMPONENT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select optional skirt, hair or soft trailer components.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AdditionalComponent.values().forEach { comp ->
                    val isSelected = config.additionalComponent.equals(comp.displayName, ignoreCase = true)
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
                            .clickable { viewModel.updateAdditionalComponent(comp.displayName) }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(comp.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(comp.technicalSpec, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
}

// -------------------------------------------------------------
// STEP 5: SPECIAL FEATURES & ADVANCED SPECS
// -------------------------------------------------------------
@Composable
private fun FeaturesStepContent(
    currentConfig: ProductConfiguration,
    configViewModel: ConfiguratorViewModel,
    showAdvanced: Boolean,
    onToggleAdvanced: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // SPECIAL FEATURES TOGGLES
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SPECIAL FEATURES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FeatureToggleRow(
                    title = "Phosphorescent Glow Effect",
                    subtitle = "Charges under sunlight/UV for deep sea visibility",
                    checked = currentConfig.hasGlow,
                    onCheckedChange = {
                        configViewModel.updateSpecialFeatures(it, currentConfig.hasUvReactive, currentConfig.hasRattle, currentConfig.hasWeedGuard, currentConfig.customMarking)
                    }
                )

                FeatureToggleRow(
                    title = "UV Reactive Fluorescence",
                    subtitle = "Highlights edge silhouette under ambient marine light",
                    checked = currentConfig.hasUvReactive,
                    onCheckedChange = {
                        configViewModel.updateSpecialFeatures(currentConfig.hasGlow, it, currentConfig.hasRattle, currentConfig.hasWeedGuard, currentConfig.customMarking)
                    }
                )

                FeatureToggleRow(
                    title = "Internal Acoustic Rattle",
                    subtitle = "Embedded stainless steel acoustic chambers for vibration",
                    checked = currentConfig.hasRattle,
                    onCheckedChange = {
                        configViewModel.updateSpecialFeatures(currentConfig.hasGlow, currentConfig.hasUvReactive, it, currentConfig.hasWeedGuard, currentConfig.customMarking)
                    }
                )

                FeatureToggleRow(
                    title = "Nylon Weed Guard",
                    subtitle = "Multi-strand fiber guard protecting hook from snagging",
                    checked = currentConfig.hasWeedGuard,
                    onCheckedChange = {
                        configViewModel.updateSpecialFeatures(currentConfig.hasGlow, currentConfig.hasUvReactive, currentConfig.hasRattle, it, currentConfig.customMarking)
                    }
                )
            }
        }

        // ADVANCED PRODUCTION SPECIFICATIONS (Default OFF per Section 45)
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ADVANCED SPECIFICATIONS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Deeper engineering & tolerance parameters",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showAdvanced,
                        onCheckedChange = onToggleAdvanced
                    )
                }

                if (showAdvanced) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Revision Control (Rev A, Rev B, Rev C)
                    Text("Drawing Revision", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("A", "B", "C").forEach { rev ->
                            val isSelected = currentConfig.revision == rev
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateRevision(rev) },
                                label = { Text("Revision $rev") }
                            )
                        }
                    }

                    // Drawing Status (Draft, For Review, Approved)
                    Text("Production Drawing Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DRAFT", "FOR REVIEW", "APPROVED").forEach { st ->
                            val isSelected = currentConfig.drawingStatus == st
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateDrawingStatus(st) },
                                label = { Text(st) }
                            )
                        }
                    }

                    // Tolerances (Default TBD per Section 34)
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

                    // Material Grade / Coating
                    OutlinedTextField(
                        value = currentConfig.densityGrade,
                        onValueChange = { configViewModel.updateAdvancedProductionSpecs(densityGrade = it) },
                        label = { Text("Material Grade / Metallurgy") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = currentConfig.coating,
                        onValueChange = { configViewModel.updateAdvancedProductionSpecs(coating = it) },
                        label = { Text("Surface Protective Coating") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
internal fun FeatureToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// -------------------------------------------------------------
// STEP 6: REVIEW & SUMMARY
// -------------------------------------------------------------
@Composable
private fun ReviewStepContent(
    currentConfig: ProductConfiguration,
    validationIssue: SpecValidation.ValidationIssue?,
    onEdit: () -> Unit,
    onProceedToDrawing: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Validation Notice if any
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

        // SUMMARY CARD (Section 18)
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CONFIGURATION SUMMARY",
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
                    "Shape" to currentConfig.shape,
                    "Target Weight" to "${currentConfig.weightGrams.toInt()} g",
                    "Dimensions" to "${currentConfig.lengthMm.toInt()}L x ${currentConfig.widthMm.toInt()}W x ${currentConfig.heightMm.toInt()}H mm",
                    "Body Material" to currentConfig.material,
                    "Color Theme" to "${currentConfig.colorName}${if (currentConfig.hasAccentColor) " (with Accent)" else ""}",
                    "Surface Finish" to currentConfig.finishType,
                    "Pattern" to currentConfig.patternName,
                    "Eye Style" to "${currentConfig.eyeStyle} (${currentConfig.eyeShape})",
                    "Hook" to "${currentConfig.hookSize} ${currentConfig.hookLevel} (${currentConfig.hookStyle})",
                    "Additional Component" to currentConfig.additionalComponent,
                    "Special Features" to listOfNotNull(
                        if (currentConfig.hasGlow) "Glow" else null,
                        if (currentConfig.hasUvReactive) "UV" else null,
                        if (currentConfig.hasRattle) "Rattle" else null,
                        if (currentConfig.hasWeedGuard) "Weed Guard" else null
                    ).ifEmpty { listOf("None") }.joinToString(", ")
                )

                summaryItems.forEach { (k, v) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(k, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(v, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    Button(
                        onClick = onProceedToDrawing,
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.DesignServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Technical Drawing")
                    }
                }
            }
        }

        // CAD SCHEMATIC PREVIEW
        TactileCard(modifier = Modifier.fillMaxWidth()) {
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

        // FISHING ANIMATION
        TactileCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "PRODUCT IN-USE DEMONSTRATION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                JigFishingAnimation(config = currentConfig)
            }
        }
    }
}
