package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ai.GeminiImageService
import com.example.data.geometry.JigGeometryEngine
import com.example.data.model.JigConfiguration
import com.example.data.model.JigShapeRepository
import com.example.ui.components.AppHeader
import com.example.ui.components.JigEngineeringCanvas
import com.example.ui.components.JigLiveCanvasPreview
import com.example.viewmodel.ConfiguratorViewModel
import java.io.File

/**
 * 7Hooks Progressive Shape-First Jig Configurator Screen.
 *
 * Flow:
 * - Selected shape remains permanently visible as the design foundation
 * - Progressive 10-step wizard:
 *   01: Size (Weight) -> 02: Length -> 03: Width -> 04: Color -> 05: Pattern ->
 *   06: Finish -> 07: Eye -> 08: Assist Hook -> 09: Assist Cord -> 10: Rings
 * - Review Screen (Step 11): Inspect specs + trigger Gemini Image Generation
 * - Final Result Screen (Step 12): AI Render vs CAD Blueprint + Save + PDF Export
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JigConfigScreen(
    configViewModel: ConfiguratorViewModel,
    onNavigateToEngineering: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentStep by configViewModel.currentConfigStep.collectAsState()
    val jigConfig by configViewModel.currentJigConfig.collectAsState()
    val isGeneratingAi by configViewModel.isGeneratingAi.collectAsState()
    val aiResult by configViewModel.aiGenerationResult.collectAsState()
    val isBlueprintView by configViewModel.isAiResultViewBlueprint.collectAsState()
    val isGeneratingPdf by configViewModel.isGeneratingPdf.collectAsState()
    val pdfResult by configViewModel.pdfValidationResult.collectAsState()
    val saveMessage by configViewModel.saveStatusMessage.collectAsState()

    val currentProductConfig by configViewModel.currentConfig.collectAsState()
    val shapeTemplate = remember(jigConfig.shapeId) {
        JigShapeRepository.getById(jigConfig.shapeId)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(saveMessage) {
        saveMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            configViewModel.clearStatusMessage()
        }
    }

    LaunchedEffect(pdfResult) {
        pdfResult?.let { res ->
            if (res.isValid) {
                snackbarHostState.showSnackbar("A4 PDF Specification Exported: ${res.file.name}")
            }
        }
    }

    Scaffold(
        topBar = {
            val titleText = when (currentStep) {
                in 1..10 -> "Step 0$currentStep/10: ${shapeTemplate.shapeName}"
                11 -> "Review Specification"
                12 -> "Final Product Render"
                else -> "Jig Configurator"
            }

            AppHeader(
                title = titleText,
                showBackButton = true,
                onBackClick = {
                    if (currentStep > 1) {
                        configViewModel.previousConfigStep()
                    } else {
                        onNavigateBack()
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8FAFC),
        modifier = Modifier.testTag("jig_progressive_config_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // COMPACT PROGRESS BAR (Section 9)
            if (currentStep in 1..10) {
                val progress = currentStep / 10f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Color(0xFF0284C7),
                    trackColor = Color(0xFFE2E8F0)
                )

                // LIVE SPECIFICATION SUMMARY PILL (Section 56)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${shapeTemplate.shapeName} • ${jigConfig.weightGrams.toInt()}g • ${jigConfig.lengthMm.toInt()}mm",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${jigConfig.mainColor} / ${jigConfig.pattern}",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // CENTER: LIVE PREVIEW CANVAS (Always visible, Section 5 & 10)
            if (currentStep <= 11) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    JigLiveCanvasPreview(
                        config = jigConfig,
                        activeStep = currentStep,
                        showControls = true
                    )
                }
            }

            // BOTTOM: PROGRESSIVE STEP CONTENT / REVIEW / RESULT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (currentStep) {
                    in 1..10 -> {
                        // Progressive Step Wizard
                        AnimatedContent(
                            targetState = currentStep,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally(animationSpec = tween(220)) { it } + fadeIn())
                                        .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { -it } + fadeOut())
                                } else {
                                    (slideInHorizontally(animationSpec = tween(220)) { -it } + fadeIn())
                                        .togetherWith(slideOutHorizontally(animationSpec = tween(220)) { it } + fadeOut())
                                }
                            },
                            label = "step_transition"
                        ) { step ->
                            StepContainer(
                                step = step,
                                config = jigConfig,
                                template = shapeTemplate,
                                viewModel = configViewModel
                            )
                        }
                    }

                    11 -> {
                        // Step 11: Review Specifications & Trigger Gemini AI Render
                        ReviewAndGenerateView(
                            config = jigConfig,
                            template = shapeTemplate,
                            isGenerating = isGeneratingAi,
                            result = aiResult,
                            onEditClick = { targetStep -> configViewModel.setConfigStep(targetStep) },
                            onGenerateClick = { configViewModel.generateFinalAiProduct() }
                        )
                    }

                    12 -> {
                        // Step 12: Final Product Result Screen
                        FinalProductResultView(
                            config = jigConfig,
                            productConfig = currentProductConfig,
                            template = shapeTemplate,
                            isBlueprintView = isBlueprintView,
                            isGeneratingAi = isGeneratingAi,
                            isGeneratingPdf = isGeneratingPdf,
                            aiResult = aiResult,
                            onToggleView = { configViewModel.toggleAiResultView() },
                            onRegenerate = { configViewModel.generateFinalAiProduct(forceRegenerate = true) },
                            onSave = { configViewModel.saveCurrentConfig() },
                            onExportPdf = { configViewModel.generatePdf(context) },
                            onOpenEngineering = onNavigateToEngineering,
                            onEdit = { configViewModel.setConfigStep(10) }
                        )
                    }
                }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Configuration?") },
            text = { Text("This will reset all dimensions, finishes, and hardware options back to the base template defaults.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        configViewModel.resetJigConfiguration()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getStepTitle(step: Int): String {
    return when (step) {
        1 -> "Size & Target Weight"
        2 -> "Total Length"
        3 -> "Body Width"
        4 -> "Color Scheme"
        5 -> "Attraction Pattern"
        6 -> "Surface Finish"
        7 -> "3D Strike Eye"
        8 -> "Assist Hook Rig"
        9 -> "Assist Cord"
        10 -> "Solid Rings"
        else -> "Configuration"
    }
}

/**
 * Step Container providing scrolling options and bottom Prev/Next navigation bar.
 */
@Composable
private fun StepContainer(
    step: Int,
    config: JigConfiguration,
    template: com.example.data.model.JigShapeTemplate,
    viewModel: ConfiguratorViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Step Options Form (Scrollable)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (step) {
                1 -> SizeStep(config, template, viewModel)
                2 -> LengthStep(config, template, viewModel)
                3 -> WidthStep(config, template, viewModel)
                4 -> ColorStep(config, template, viewModel)
                5 -> PatternStep(config, template, viewModel)
                6 -> FinishStep(config, template, viewModel)
                7 -> EyeStep(config, template, viewModel)
                8 -> AssistHookStep(config, template, viewModel)
                9 -> AssistCordStep(config, template, viewModel)
                10 -> RingsStep(config, template, viewModel)
            }
        }

        // Bottom Wizard Navigation Row
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { viewModel.previousConfigStep() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF334155))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = { viewModel.nextConfigStep() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text(
                        text = if (step == 10) "Review Specs" else "Next Step",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// STEP IMPLEMENTATIONS (01 to 10)
// -----------------------------------------------------------------------------

@Composable
private fun SizeStep(config: JigConfiguration, template: com.example.data.model.JigShapeTemplate, viewModel: ConfiguratorViewModel) {
    val presets = listOf(40f, 60f, 80f, 100f, 120f, 150f, 200f)
    var isCustom by remember { mutableStateOf(config.customValues.containsKey("weight")) }
    var customText by remember { mutableStateOf(config.customValues["weight"] ?: "") }

    Text("TARGET CASTING WEIGHT", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Text("Select standard gram weight or specify custom ballistic ballast.", fontSize = 13.sp, color = Color(0xFF64748B))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { weight ->
            val isSelected = !isCustom && config.weightGrams == weight
            FilterChip(
                selected = isSelected,
                onClick = {
                    isCustom = false
                    viewModel.updateJigWeight(weight)
                },
                label = { Text("${weight.toInt()} g", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }

        FilterChip(
            selected = isCustom,
            onClick = { isCustom = true },
            label = { Text("Custom...") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
        )
    }

    if (isCustom) {
        OutlinedTextField(
            value = customText,
            onValueChange = {
                customText = it
                val parsed = it.toFloatOrNull()
                if (parsed != null && parsed in 10f..350f) {
                    viewModel.updateJigWeight(parsed, custom = it)
                }
            },
            label = { Text("Custom Weight (10 - 350 grams)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }

    Slider(
        value = config.weightGrams.coerceIn(template.minWeightGrams, template.maxWeightGrams),
        onValueChange = {
            isCustom = false
            viewModel.updateJigWeight(it)
        },
        valueRange = template.minWeightGrams..template.maxWeightGrams,
        steps = 19
    )
}

@Composable
private fun LengthStep(config: JigConfiguration, template: com.example.data.model.JigShapeTemplate, viewModel: ConfiguratorViewModel) {
    val presets = listOf(80f, 100f, 115f, 130f, 150f, 180f, 210f)
    var isCustom by remember { mutableStateOf(config.customValues.containsKey("length")) }
    var customText by remember { mutableStateOf(config.customValues["length"] ?: "") }

    Text("OVERALL LENGTH", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Text("Hydrodynamic body chord length from front ring anchor to rear eyelet.", fontSize = 13.sp, color = Color(0xFF64748B))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { length ->
            val isSelected = !isCustom && config.lengthMm == length
            FilterChip(
                selected = isSelected,
                onClick = {
                    isCustom = false
                    viewModel.updateJigLength(length)
                },
                label = { Text("${length.toInt()} mm", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }

        FilterChip(
            selected = isCustom,
            onClick = { isCustom = true },
            label = { Text("Custom...") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
        )
    }

    if (isCustom) {
        OutlinedTextField(
            value = customText,
            onValueChange = {
                customText = it
                val parsed = it.toFloatOrNull()
                if (parsed != null && parsed in 40f..300f) {
                    viewModel.updateJigLength(parsed, custom = it)
                }
            },
            label = { Text("Custom Length (40 - 300 mm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }

    Slider(
        value = config.lengthMm.coerceIn(template.minLengthMm, template.maxLengthMm),
        onValueChange = {
            isCustom = false
            viewModel.updateJigLength(it)
        },
        valueRange = template.minLengthMm..template.maxLengthMm,
        steps = 19
    )
}

@Composable
private fun WidthStep(config: JigConfiguration, template: com.example.data.model.JigShapeTemplate, viewModel: ConfiguratorViewModel) {
    val presets = listOf(14f, 18f, 20f, 22f, 26f, 30f)
    var isCustom by remember { mutableStateOf(config.customValues.containsKey("width")) }
    var customText by remember { mutableStateOf(config.customValues["width"] ?: "") }

    Text("MAXIMUM BODY WIDTH / KEEL BEAM", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Text("Controls lateral water displacement and fluttering cadence.", fontSize = 13.sp, color = Color(0xFF64748B))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { width ->
            val isSelected = !isCustom && config.widthMm == width
            FilterChip(
                selected = isSelected,
                onClick = {
                    isCustom = false
                    viewModel.updateJigWidth(width)
                },
                label = { Text("${width.toInt()} mm", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }

        FilterChip(
            selected = isCustom,
            onClick = { isCustom = true },
            label = { Text("Custom...") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
        )
    }

    if (isCustom) {
        OutlinedTextField(
            value = customText,
            onValueChange = {
                customText = it
                val parsed = it.toFloatOrNull()
                if (parsed != null && parsed in 8f..50f) {
                    viewModel.updateJigWidth(parsed, custom = it)
                }
            },
            label = { Text("Custom Width (8 - 50 mm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }

    Slider(
        value = config.widthMm.coerceIn(template.minWidthMm, template.maxWidthMm),
        onValueChange = {
            isCustom = false
            viewModel.updateJigWidth(it)
        },
        valueRange = template.minWidthMm..template.maxWidthMm,
        steps = 15
    )
}

@Composable
private fun ColorStep(config: JigConfiguration, template: com.example.data.model.JigShapeTemplate, viewModel: ConfiguratorViewModel) {
    val palette = listOf(
        Pair("Tournament Orange", 0xFFEA580CL),
        Pair("Deep Sea Blue", 0xFF0284C7L),
        Pair("Solar Yellow", 0xFFEAB308L),
        Pair("Stealth Black", 0xFF0F172AL),
        Pair("Cyber Yellow", 0xFFFACC15L),
        Pair("Blossom Pink", 0xFFEC4899L),
        Pair("Chrome Silver", 0xFFCBD5E1L),
        Pair("Emerald Green", 0xFF10B981L),
        Pair("Pure Pearl", 0xFFF8FAFCL)
    )

    Text("PRIMARY DORSAL COLOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        palette.forEach { (name, hex) ->
            val isSelected = config.mainColor == name
            FilterChip(
                selected = isSelected,
                onClick = { viewModel.updateJigColors(name, hex, config.secondaryColor, config.secondaryColorHex) },
                label = { Text(name, fontSize = 11.sp) },
                leadingIcon = {
                    Box(modifier = Modifier.size(14.dp).background(Color(hex), CircleShape).border(1.dp, Color.Gray, CircleShape))
                },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text("SECONDARY KEEL / ACCENT COLOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        palette.forEach { (name, hex) ->
            val isSelected = config.secondaryColor == name
            FilterChip(
                selected = isSelected,
                onClick = { viewModel.updateJigColors(config.mainColor, config.mainColorHex, name, hex) },
                label = { Text(name, fontSize = 11.sp) },
                leadingIcon = {
                    Box(modifier = Modifier.size(14.dp).background(Color(hex), CircleShape).border(1.dp, Color.Gray, CircleShape))
                },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }
    }
}

@Composable
private fun PatternStep(config: JigConfiguration, template: com.example.data.model.JigShapeTemplate, viewModel: ConfiguratorViewModel) {
    val patterns = listOf("Solid", "Tiger", "Dots", "Chevron", "Scales", "Zebra")
    val patternColors = listOf(
        Pair("Stealth Black", 0xFF0F172AL),
        Pair("Chrome Silver", 0xFFCBD5E1L),
        Pair("Deep Sea Blue", 0xFF0284C7L),
        Pair("Luminous Lime", 0xFF84CC16L),
        Pair("Solar Yellow", 0xFFEAB308L),
        Pair("Blaze Orange", 0xFFEA580CL)
    )

    Text("TACTICAL ATTRACTION PATTERN", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        patterns.forEach { pat ->
            val isSelected = config.pattern.equals(pat, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { viewModel.updateJigPattern(pat, config.patternColor, config.patternColorHex) },
                label = { Text(pat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }
    }

    if (config.pattern != "Solid") {
        Spacer(modifier = Modifier.height(6.dp))
        Text("PATTERN CONTRAST COLOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            patternColors.forEach { (name, hex) ->
                val isSelected = config.patternColor == name
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateJigPattern(config.pattern, name, hex) },
                    label = { Text(name, fontSize = 11.sp) },
                    leadingIcon = {
                        Box(modifier = Modifier.size(14.dp).background(Color(hex), CircleShape).border(1.dp, Color.Gray, CircleShape))
                    },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
                )
            }
        }
    }
}

@Composable
private fun FinishStep(config: JigConfiguration, template: com.example.data.model.JigShapeTemplate, viewModel: ConfiguratorViewModel) {
    val finishes = listOf("Metallic", "Gloss", "Matte", "Holographic", "Glitter", "Glow", "UV Reactive")
    var isCustom by remember { mutableStateOf(config.customValues.containsKey("finish")) }
    var customText by remember { mutableStateOf(config.customValues["finish"] ?: "") }

    Text("SURFACE COATING & FINISH", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Text("Multi-layer automotive clear-coat and light-refraction characteristics.", fontSize = 13.sp, color = Color(0xFF64748B))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        finishes.forEach { fin ->
            val isSelected = !isCustom && config.finish.equals(fin, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = {
                    isCustom = false
                    viewModel.updateJigFinish(fin)
                },
                label = { Text(fin, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }

        FilterChip(
            selected = isCustom,
            onClick = { isCustom = true },
            label = { Text("Custom...") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
        )
    }

    if (isCustom) {
        OutlinedTextField(
            value = customText,
            onValueChange = {
                customText = it
                viewModel.updateJigFinish("Custom", custom = it)
            },
            label = { Text("Custom Surface Finish Specification") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EyeStep(config: JigConfiguration, template: com.example.data.model.JigShapeTemplate, viewModel: ConfiguratorViewModel) {
    val styles = listOf("3D Strike", "Holographic", "Luminous Target")
    val colors = listOf("Ruby Red", "Emerald Green", "Solar Gold", "Chrome Silver", "Luminous Lime", "Sapphire Blue")

    Text("3D STRIKE EYE PROFILE", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        styles.forEach { st ->
            val isSelected = config.eyeStyle.equals(st, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { viewModel.updateJigEye(st, config.eyeColor) },
                label = { Text(st, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text("EYE IRIS COLOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.forEach { col ->
            val isSelected = config.eyeColor.equals(col, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { viewModel.updateJigEye(config.eyeStyle, col) },
                label = { Text(col, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }
    }
}

@Composable
private fun AssistHookStep(config: JigConfiguration, template: com.example.data.model.JigShapeTemplate, viewModel: ConfiguratorViewModel) {
    val hooks = listOf("None", "Standard Mustad 3/0", "Heavy Duty BKK 5/0", "Owner Monster 5/0", "Twin Assist Rig 3/0")
    var isCustom by remember { mutableStateOf(config.customValues.containsKey("assistHook")) }
    var customText by remember { mutableStateOf(config.customValues["assistHook"] ?: "") }

    Text("ASSIST HOOK RIGGING", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Text("Forged saltwater carbon steel hook bound to nose ring anchor.", fontSize = 13.sp, color = Color(0xFF64748B))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        hooks.forEach { hk ->
            val isSelected = !isCustom && config.assistHook == hk
            FilterChip(
                selected = isSelected,
                onClick = {
                    isCustom = false
                    viewModel.updateJigAssistHook(hk)
                },
                label = { Text(hk, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }

        FilterChip(
            selected = isCustom,
            onClick = { isCustom = true },
            label = { Text("Custom...") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
        )
    }

    if (isCustom) {
        OutlinedTextField(
            value = customText,
            onValueChange = {
                customText = it
                viewModel.updateJigAssistHook("Custom", custom = it)
            },
            label = { Text("Custom Hook Specification") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AssistCordStep(config: JigConfiguration, template: com.example.data.model.JigShapeTemplate, viewModel: ConfiguratorViewModel) {
    val cordColors = listOf("Red", "Royal Blue", "Stealth Black", "Blaze Orange", "Chartreuse Glow", "Kevlar Gold")
    var isCustom by remember { mutableStateOf(config.customValues.containsKey("assistCord")) }
    var customText by remember { mutableStateOf(config.customValues["assistCord"] ?: "") }

    Text("BRAIDED PE ASSIST CORD", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Text("200 lb high-tensile braided filament with Kevlar whipping collar.", fontSize = 13.sp, color = Color(0xFF64748B))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        cordColors.forEach { cord ->
            val isSelected = !isCustom && config.assistCordColor == cord
            FilterChip(
                selected = isSelected,
                onClick = {
                    isCustom = false
                    viewModel.updateJigAssistCord(cord)
                },
                label = { Text(cord, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }

        FilterChip(
            selected = isCustom,
            onClick = { isCustom = true },
            label = { Text("Custom...") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
        )
    }

    if (isCustom) {
        OutlinedTextField(
            value = customText,
            onValueChange = {
                customText = it
                viewModel.updateJigAssistCord("Custom", custom = it)
            },
            label = { Text("Custom Cord Material / Color") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RingsStep(config: JigConfiguration, template: com.example.data.model.JigShapeTemplate, viewModel: ConfiguratorViewModel) {
    val ringOptions = listOf("None", "Standard", "Heavy Duty")
    var isCustomFR by remember { mutableStateOf(config.customValues.containsKey("frontRing")) }
    var isCustomBR by remember { mutableStateOf(config.customValues.containsKey("backRing")) }
    var customFRText by remember { mutableStateOf(config.customValues["frontRing"] ?: "") }
    var customBRText by remember { mutableStateOf(config.customValues["backRing"] ?: "") }

    Text("FRONT SOLID NOSE RING", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ringOptions.forEach { opt ->
            val isSelected = !isCustomFR && config.frontRing == opt
            FilterChip(
                selected = isSelected,
                onClick = {
                    isCustomFR = false
                    viewModel.updateJigRings(opt, config.backRing)
                },
                label = { Text(opt) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }
        FilterChip(
            selected = isCustomFR,
            onClick = { isCustomFR = true },
            label = { Text("Custom...") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
        )
    }

    if (isCustomFR) {
        OutlinedTextField(
            value = customFRText,
            onValueChange = {
                customFRText = it
                viewModel.updateJigRings("Custom", config.backRing, customFront = it)
            },
            label = { Text("Custom Front Ring Specification") },
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text("REAR TAIL SPLIT RING", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ringOptions.forEach { opt ->
            val isSelected = !isCustomBR && config.backRing == opt
            FilterChip(
                selected = isSelected,
                onClick = {
                    isCustomBR = false
                    viewModel.updateJigRings(config.frontRing, opt)
                },
                label = { Text(opt) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }
        FilterChip(
            selected = isCustomBR,
            onClick = { isCustomBR = true },
            label = { Text("Custom...") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
        )
    }

    if (isCustomBR) {
        OutlinedTextField(
            value = customBRText,
            onValueChange = {
                customBRText = it
                viewModel.updateJigRings(config.frontRing, "Custom", customBack = it)
            },
            label = { Text("Custom Back Ring Specification") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// -----------------------------------------------------------------------------
// STEP 11: REVIEW & GENERATE VIEW (Section 30-34)
// -----------------------------------------------------------------------------

@Composable
private fun ReviewAndGenerateView(
    config: JigConfiguration,
    template: com.example.data.model.JigShapeTemplate,
    isGenerating: Boolean,
    result: GeminiImageService.GenerationResult?,
    onEditClick: (Int) -> Unit,
    onGenerateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SPECIFICATION VERIFICATION",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${template.shapeName} Hydrodynamic Jig",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Canonical Hash: ${config.configurationHash.take(16)}...",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Specification Grid Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpecRow("Base Shape", template.shapeName, 1, onEditClick)
                SpecRow("Dimensions", "${config.lengthMm.toInt()} mm × ${config.widthMm.toInt()} mm", 2, onEditClick)
                SpecRow("Target Weight", "${config.weightGrams.toInt()} g", 1, onEditClick)
                SpecRow("Color Scheme", "${config.mainColor} / ${config.secondaryColor}", 4, onEditClick)
                SpecRow("Pattern", "${config.pattern} (${config.patternColor})", 5, onEditClick)
                SpecRow("Surface Finish", config.finish, 6, onEditClick)
                SpecRow("Strike Eye", "${config.eyeStyle} (${config.eyeColor})", 7, onEditClick)
                SpecRow("Assist Hook", config.assistHook, 8, onEditClick)
                SpecRow("Assist Cord", config.assistCordColor, 9, onEditClick)
                SpecRow("Solid Rings", "Front: ${config.frontRing} • Back: ${config.backRing}", 10, onEditClick)
            }
        }

        // Error message if generation failed
        if (result is GeminiImageService.GenerationResult.Failure) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEF2F2),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFDC2626))
                    Column {
                        Text("Final AI Rendering Connection Notice", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B), fontSize = 13.sp)
                        Text(result.errorMessage, color = Color(0xFFB91C1C), fontSize = 12.sp)
                    }
                }
            }
        }

        // Prominent Action: Generate Final Product
        Button(
            onClick = onGenerateClick,
            enabled = !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("generate_final_product_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "GENERATING STUDIO RENDER...",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = Color(0xFF38BDF8))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GENERATE FINAL PRODUCT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String, stepIndex: Int, onEditClick: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), fontSize = 10.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
        }
        TextButton(
            onClick = { onEditClick(stepIndex) },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("Edit", fontSize = 12.sp, color = Color(0xFF0284C7))
        }
    }
}

// -----------------------------------------------------------------------------
// STEP 12: FINAL PRODUCT RESULT VIEW (Section 35-46)
// -----------------------------------------------------------------------------

@Composable
private fun FinalProductResultView(
    config: JigConfiguration,
    productConfig: com.example.data.model.ProductConfiguration,
    template: com.example.data.model.JigShapeTemplate,
    isBlueprintView: Boolean,
    isGeneratingAi: Boolean,
    isGeneratingPdf: Boolean,
    aiResult: GeminiImageService.GenerationResult?,
    onToggleView: () -> Unit,
    onRegenerate: () -> Unit,
    onSave: () -> Unit,
    onExportPdf: () -> Unit,
    onOpenEngineering: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Switcher: AI Studio Render vs CAD Blueprint
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FINAL PRODUCT OUTPUT",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF0F172A)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = !isBlueprintView,
                    onClick = onToggleView,
                    label = { Text("AI Studio Render", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = isBlueprintView,
                    onClick = onToggleView,
                    label = { Text("CAD Blueprint", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
                )
            }
        }

        // HERO MEDIA CANVAS (Section 35)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(16.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            if (!isBlueprintView) {
                // AI PHOTOREALISTIC STUDIO RENDER
                val imageFile = (aiResult as? GeminiImageService.GenerationResult.Success)?.file
                if (imageFile != null && imageFile.exists()) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = "Final AI Product Render",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Fallback to high-res Live Compositor if file hasn't loaded
                    JigLiveCanvasPreview(config = config, activeStep = 10, showControls = false)
                }
            } else {
                // CAD ORTHOGRAPHIC BLUEPRINT VIEW
                JigEngineeringCanvas(
                    config = productConfig,
                    perspective = JigGeometryEngine.EngineeringPerspective.FRONT_ELEVATION,
                    theme = JigGeometryEngine.EngineeringTheme.BLUEPRINT_NAVY,
                    showDimensions = true,
                    showGrid = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // High-Res Watermark Pill
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xDD0F172A)
            ) {
                Text(
                    text = "7HOOKS PRECISION TACKLE",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // SUMMARY CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "${template.shapeName} ${config.weightGrams.toInt()}g — ${config.mainColor}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Ref: ${productConfig.referenceNumber} • Model: ${productConfig.modelNumber}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "Finish: ${config.finish} • Pattern: ${config.pattern} • Rigging: ${config.assistHook} with ${config.assistCordColor} cord",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        // ACTION BUTTONS (Section 39-46)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Config", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onRegenerate,
                enabled = !isGeneratingAi,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Regenerate", fontSize = 12.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onOpenEngineering,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                Icon(imageVector = Icons.Default.Architecture, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CAD Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onExportPdf,
                enabled = !isGeneratingPdf,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) {
                if (isGeneratingPdf) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        TextButton(
            onClick = onEdit,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit Configuration", color = Color(0xFF64748B), fontSize = 13.sp)
        }
    }
}
