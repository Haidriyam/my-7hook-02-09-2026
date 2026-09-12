package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.FileProvider
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
    val coroutineScope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }
    var pendingSavePdfToStorage by remember { mutableStateOf(false) }
    var pendingSharePdf by remember { mutableStateOf(false) }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            val file = pdfResult?.file
            if (file != null && file.exists()) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        file.inputStream().use { input -> input.copyTo(out) }
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("PDF saved successfully to your selected location.")
                    }
                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error saving PDF: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    val launchSharePdf = {
        val file = pdfResult?.file
        if (file != null && file.exists()) {
            val fileUri = pdfResult?.fileUri ?: androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "7Hooks Technical Specification - ${shapeTemplate.shapeName}")
                putExtra(Intent.EXTRA_TEXT, "7Hooks Precision Tackle Engineering Blueprint for ${shapeTemplate.shapeName} (${jigConfig.weightGrams.toInt()}g).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Specification PDF"))
        }
    }

    LaunchedEffect(saveMessage) {
        saveMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            configViewModel.clearStatusMessage()
        }
    }

    LaunchedEffect(pdfResult) {
        pdfResult?.let { res ->
            if (res.isValid) {
                if (pendingSavePdfToStorage) {
                    pendingSavePdfToStorage = false
                    val sanitized = shapeTemplate.shapeName.replace(" ", "_")
                    savePdfLauncher.launch("7Hooks_${sanitized}_${jigConfig.weightGrams.toInt()}g_Spec.pdf")
                } else if (pendingSharePdf) {
                    pendingSharePdf = false
                    launchSharePdf()
                } else {
                    snackbarHostState.showSnackbar("A4 PDF Specification Exported: ${res.file.name}")
                }
            }
        }
    }

    // GUARANTEE: At the end of jig config, an image must be generated
    LaunchedEffect(currentStep, jigConfig.configurationHash) {
        if (currentStep >= 11 && aiResult == null && !isGeneratingAi) {
            configViewModel.generateFinalAiProduct()
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
                    if (currentStep == 12) {
                        configViewModel.setConfigStep(10)
                    } else if (currentStep > 1) {
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

                // LIVE SPECIFICATION SUMMARY PILL (Progressive Disclosure)
                val (specLeft, specRight) = when {
                    currentStep == 1 -> Pair("${shapeTemplate.shapeName} • Weight: ${jigConfig.weightGrams.toInt()}g", "Stage 1: Weight")
                    currentStep == 2 -> Pair("${shapeTemplate.shapeName} • ${jigConfig.weightGrams.toInt()}g • ${jigConfig.lengthMm.toInt()}mm", "Stage 2: Length")
                    currentStep == 3 -> Pair("${shapeTemplate.shapeName} • ${jigConfig.lengthMm.toInt()}×${jigConfig.widthMm.toInt()}mm", "Stage 3: Keel Beam")
                    currentStep == 4 -> Pair("${shapeTemplate.shapeName} • ${jigConfig.mainColor}", if (jigConfig.hasDualTone) "Dual-Tone Keel" else "Stage 4: Paint")
                    currentStep == 5 -> Pair("${jigConfig.mainColor} / ${jigConfig.secondaryColor}", "Pattern: ${jigConfig.pattern}")
                    currentStep == 6 -> Pair("${jigConfig.mainColor} • ${jigConfig.finish}", "Finish: ${jigConfig.finish}")
                    currentStep == 7 -> Pair("Eye: ${jigConfig.eyeStyle} (${jigConfig.eyeSize})", "Iris: ${jigConfig.eyeColor}")
                    currentStep == 8 -> Pair("Rigging: ${jigConfig.assistHook}", "Hook Rig")
                    currentStep == 9 -> Pair("Cord: ${jigConfig.assistCordColor}", "200lb Braided PE")
                    currentStep == 10 -> Pair("Rings: Front, Rear, Top, Bottom", "Size: ${jigConfig.ringSize}")
                    else -> Pair("${shapeTemplate.shapeName} • ${jigConfig.weightGrams.toInt()}g • ${jigConfig.lengthMm.toInt()}mm", "${jigConfig.mainColor} / ${jigConfig.pattern}")
                }
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
                            text = specLeft,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = specRight,
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
                            onGenerateClick = { configViewModel.generateFinalAiProduct() },
                            onNavigateToResult = { configViewModel.setConfigStep(12) }
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
                            onGenerateWithPrompt = { prompt -> configViewModel.generateFinalAiProduct(forceRegenerate = true, customPrompt = prompt) },
                            onSave = { configViewModel.saveCurrentConfig() },
                            onExportPdf = { configViewModel.generatePdf(context) },
                            onSavePdfToStorage = {
                                if (pdfResult?.file?.exists() == true) {
                                    val sanitized = shapeTemplate.shapeName.replace(" ", "_")
                                    savePdfLauncher.launch("7Hooks_${sanitized}_${jigConfig.weightGrams.toInt()}g_Spec.pdf")
                                } else {
                                    pendingSavePdfToStorage = true
                                    configViewModel.generatePdf(context)
                                }
                            },
                            onSharePdf = {
                                if (pdfResult?.file?.exists() == true) {
                                    launchSharePdf()
                                } else {
                                    pendingSharePdf = true
                                    configViewModel.generatePdf(context)
                                }
                            },
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

                if (step == 10) {
                    Button(
                        onClick = {
                            viewModel.nextConfigStep()
                            viewModel.generateFinalAiProduct()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.testTag("complete_and_generate_button")
                    ) {
                        Text(
                            text = "Complete & Generate Image",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Generate Image",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.nextConfigStep() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text(
                            text = "Next Step",
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
    data class ColorCombo(
        val name: String,
        val mainName: String,
        val mainHex: Long,
        val secName: String,
        val secHex: Long,
        val pattern: String,
        val patName: String,
        val patHex: Long
    )

    val curatedCombos = listOf(
        ColorCombo("Sardine Pelagic", "Deep Ocean Blue", 0xFF0284C7L, "Chrome Silver", 0xFFCBD5E1L, "Dots", "Stealth Black", 0xFF0F172AL),
        ColorCombo("Mackerel Strike", "Emerald Mackerel", 0xFF10B981L, "Solar Gold", 0xFFEAB308L, "Zebra", "Stealth Black", 0xFF0F172AL),
        ColorCombo("Red Head Ghost", "Crimson Red", 0xFFDC2626L, "Pure Pearl", 0xFFF8FAFCL, "Solid", "Pure Pearl", 0xFFF8FAFCL),
        ColorCombo("Pink Glow Flasher", "Blossom Pink", 0xFFEC4899L, "Chrome Silver", 0xFFCBD5E1L, "Zebra", "Pure Pearl", 0xFFF8FAFCL),
        ColorCombo("Chartreuse Dorado", "Cyber Chartreuse", 0xFF84CC16L, "Solar Gold", 0xFFEAB308L, "Tiger", "Stealth Black", 0xFF0F172AL),
        ColorCombo("Midnight Squid", "Stealth Black", 0xFF0F172AL, "Electric UV Violet", 0xFF8B5CF6L, "Chevron", "Chrome Silver", 0xFFCBD5E1L),
        ColorCombo("Firetiger Predator", "Blaze Orange", 0xFFEA580CL, "Solar Gold", 0xFFEAB308L, "Tiger", "Stealth Black", 0xFF0F172AL),
        ColorCombo("Zebra Glow Dual", "Pure Pearl", 0xFFF8FAFCL, "Chrome Silver", 0xFFCBD5E1L, "Zebra", "Luminous Glow", 0xFF4ADE80L),
        ColorCombo("Bluefin Bullet", "Abyssal Navy", 0xFF1E3A8AL, "Chrome Silver", 0xFFCBD5E1L, "Chevron", "Chrome Silver", 0xFFCBD5E1L),
        ColorCombo("Blood Baitfish", "Tuna Blood Red", 0xFF991B1BL, "Chrome Silver", 0xFFCBD5E1L, "Scales", "Stealth Black", 0xFF0F172AL)
    )

    val palette = listOf(
        Pair("Deep Ocean Blue", 0xFF0284C7L),
        Pair("Abyssal Navy", 0xFF1E3A8AL),
        Pair("Emerald Mackerel", 0xFF10B981L),
        Pair("Tournament Orange", 0xFFEA580CL),
        Pair("Crimson Red", 0xFFDC2626L),
        Pair("Tuna Blood Red", 0xFF991B1BL),
        Pair("Solar Gold", 0xFFEAB308L),
        Pair("Cyber Chartreuse", 0xFF84CC16L),
        Pair("Blossom Pink", 0xFFEC4899L),
        Pair("Electric UV Violet", 0xFF8B5CF6L),
        Pair("Chrome Silver", 0xFFCBD5E1L),
        Pair("Stealth Black", 0xFF0F172AL),
        Pair("Pure Pearl", 0xFFF8FAFCL),
        Pair("Luminous Glow", 0xFF4ADE80L)
    )

    Text("⚡ CURATED TOURNAMENT COLOR COMBOS (MIXED & STRIPES)", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Text("Pre-engineered hydrodynamic patterns featuring two-tone color blending and contrast stripes.", fontSize = 12.sp, color = Color(0xFF64748B))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        curatedCombos.forEach { combo ->
            val isSelected = config.colorComboName == combo.name ||
                    (config.mainColor == combo.mainName && config.secondaryColor == combo.secName && config.pattern == combo.pattern)
            FilterChip(
                selected = isSelected,
                onClick = {
                    viewModel.applyColorCombo(
                        comboName = combo.name,
                        mainColor = combo.mainName,
                        mainHex = combo.mainHex,
                        secondaryColor = combo.secName,
                        secondaryHex = combo.secHex,
                        pattern = combo.pattern,
                        patternColor = combo.patName,
                        patternColorHex = combo.patHex
                    )
                },
                label = { Text(combo.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                leadingIcon = {
                    Row(modifier = Modifier.size(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(combo.mainHex), CircleShape))
                        Box(modifier = Modifier.size(8.dp).background(Color(combo.secHex), CircleShape))
                    }
                },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0F172A), selectedLabelColor = Color.White)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
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
    Text("SECONDARY KEEL / BELLY BLEND COLOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
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
    val styles = listOf("3D Strike", "Holographic", "Luminous Target", "Realist Fish Pupil")
    val sizes = listOf("Small (5 mm)", "Medium (8 mm)", "Large (12 mm)", "Magnum (15 mm)")
    val colors = listOf("Ruby Red", "Emerald Green", "Solar Gold", "Chrome Silver", "Luminous Lime", "Sapphire Blue", "Amethyst UV", "Pure Pearl")

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
                onClick = { viewModel.updateJigEye(st, config.eyeColor, config.eyeSize) },
                label = { Text(st, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text("EYE DIAMETER / SIZE", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sizes.forEach { sz ->
            val isSelected = config.eyeSize.equals(sz, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { viewModel.updateJigEye(config.eyeStyle, config.eyeColor, sz) },
                label = { Text(sz, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
                onClick = { viewModel.updateJigEye(config.eyeStyle, col, config.eyeSize) },
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
    val ringSizes = listOf(
        "#4 (4.5 mm • 50 lb Light)",
        "#5 (5.5 mm • 80 lb Standard)",
        "#6 (6.5 mm • 120 lb Offshore)",
        "#7 (7.5 mm • 180 lb Tuna Grade)",
        "#8 (8.5 mm • 250 lb Giant Trevally)"
    )
    val ringOptions = listOf("None", "Standard", "Heavy Duty")
    val topRingOptions = listOf("None", "Center Dorsal #5", "Forward Dorsal #7")
    val bottomRingOptions = listOf("None", "Center Keel #5", "Rear Keel #7")

    var isCustomFR by remember { mutableStateOf(config.customValues.containsKey("frontRing")) }
    var isCustomBR by remember { mutableStateOf(config.customValues.containsKey("backRing")) }
    var customFRText by remember { mutableStateOf(config.customValues["frontRing"] ?: "") }
    var customBRText by remember { mutableStateOf(config.customValues["backRing"] ?: "") }

    Text("HARDWARE RING SIZE & STRENGTH", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Text("Seamless forged 304 marine stainless steel rings scaled to withstand targeted pelagic species.", fontSize = 12.sp, color = Color(0xFF64748B))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ringSizes.forEach { sz ->
            val isSelected = config.ringSize.equals(sz, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = {
                    viewModel.updateJigRingsAll(
                        frontRing = config.frontRing,
                        backRing = config.backRing,
                        topRing = config.topRing,
                        bottomRing = config.bottomRing,
                        ringSize = sz
                    )
                },
                label = { Text(sz, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
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
                    viewModel.updateJigRingsAll(
                        frontRing = opt,
                        backRing = config.backRing,
                        topRing = config.topRing,
                        bottomRing = config.bottomRing,
                        ringSize = config.ringSize
                    )
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
                viewModel.updateJigRingsAll(
                    frontRing = "Custom",
                    backRing = config.backRing,
                    topRing = config.topRing,
                    bottomRing = config.bottomRing,
                    ringSize = config.ringSize,
                    customFront = it
                )
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
                    viewModel.updateJigRingsAll(
                        frontRing = config.frontRing,
                        backRing = opt,
                        topRing = config.topRing,
                        bottomRing = config.bottomRing,
                        ringSize = config.ringSize
                    )
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
                viewModel.updateJigRingsAll(
                    frontRing = config.frontRing,
                    backRing = "Custom",
                    topRing = config.topRing,
                    bottomRing = config.bottomRing,
                    ringSize = config.ringSize,
                    customBack = it
                )
            },
            label = { Text("Custom Back Ring Specification") },
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text("TOP DORSAL BALANCE RING", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF0284C7))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        topRingOptions.forEach { opt ->
            val isSelected = config.topRing == opt
            FilterChip(
                selected = isSelected,
                onClick = {
                    viewModel.updateJigRingsAll(
                        frontRing = config.frontRing,
                        backRing = config.backRing,
                        topRing = opt,
                        bottomRing = config.bottomRing,
                        ringSize = config.ringSize
                    )
                },
                label = { Text(opt) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF0284C7), selectedLabelColor = Color.White)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text("BOTTOM VENTRAL KEEL RING", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        bottomRingOptions.forEach { opt ->
            val isSelected = config.bottomRing == opt
            FilterChip(
                selected = isSelected,
                onClick = {
                    viewModel.updateJigRingsAll(
                        frontRing = config.frontRing,
                        backRing = config.backRing,
                        topRing = config.topRing,
                        bottomRing = opt,
                        ringSize = config.ringSize
                    )
                },
                label = { Text(opt) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White)
            )
        }
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
    onGenerateClick: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    // Auto-trigger generation upon entering review step if not already generated
    LaunchedEffect(Unit) {
        if (result == null && !isGenerating) {
            onGenerateClick()
        }
    }

    // Auto-advance to final product result screen once active generation succeeds
    var hasAutoAdvanced by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isGenerating) {
        if (!isGenerating && result is GeminiImageService.GenerationResult.Success && !hasAutoAdvanced) {
            hasAutoAdvanced = true
            onNavigateToResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isGenerating) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth().testTag("ai_rendering_banner")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                        Column {
                            Text(
                                text = "STUDIO RENDER GENERATION ACTIVE",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Generating Photorealistic Studio Render...",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Synthesizing dynamic lighting, physical specular reflections, braided assist rigging, and 7Hooks CAD geometry.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Image Preview if already generated
        if (result is GeminiImageService.GenerationResult.Success) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToResult() }
                    .testTag("review_generated_image_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0E3A68))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (result.isAiGenerated) "✨ PHOTOREALISTIC STUDIO RENDER" else "PRECISION CAD RENDER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF0E3A68),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "View Output Screen →",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF0E3A68)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = result.bitmap.asImageBitmap(),
                            contentDescription = "Generated Jig Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        text = result.statusNote.ifBlank { "Generated by 7Hooks Studio" },
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

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
                        Text("Rendering Notice", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B), fontSize = 13.sp)
                        Text(result.errorMessage, color = Color(0xFFB91C1C), fontSize = 12.sp)
                    }
                }
            }
        }

        // Prominent Actions
        if (result is GeminiImageService.GenerationResult.Success) {
            Button(
                onClick = onNavigateToResult,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("view_final_result_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E3A68)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VIEW FINAL PRODUCT (IMAGE READY)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            OutlinedButton(
                onClick = onGenerateClick,
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("regenerate_render_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF0E3A68))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RE-GENERATE STUDIO RENDER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0E3A68)
                )
            }
        } else {
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
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GENERATE FINAL PRODUCT IMAGE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
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
            Text("Edit", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
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
    onGenerateWithPrompt: (String) -> Unit,
    onSave: () -> Unit,
    onExportPdf: () -> Unit,
    onSavePdfToStorage: () -> Unit,
    onSharePdf: () -> Unit,
    onOpenEngineering: () -> Unit,
    onEdit: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var promptText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP HEADER & SEGMENTED VIEW SWITCHER
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A))
                    )
                    Text(
                        text = "FINAL PRODUCT SPECIFICATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF0E3A68),
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE2EBF5)
                ) {
                    Text(
                        text = "PRODUCTION READY",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF0E3A68)
                    )
                }
            }

            // High-precision Segmented Switcher
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF1F5F9),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isRenderSelected = !isBlueprintView
                    val isCadSelected = isBlueprintView

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isRenderSelected) onToggleView() }
                            .testTag("segment_studio_render"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isRenderSelected) Color(0xFF0E3A68) else Color.Transparent,
                        shadowElevation = if (isRenderSelected) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = if (isRenderSelected) Color.White else Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Studio Render",
                                fontSize = 12.sp,
                                fontWeight = if (isRenderSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isRenderSelected) Color.White else Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isCadSelected) onToggleView() }
                            .testTag("segment_cad_blueprint"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCadSelected) Color(0xFF0E3A68) else Color.Transparent,
                        shadowElevation = if (isCadSelected) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Architecture,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = if (isCadSelected) Color.White else Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CAD Blueprint",
                                fontSize = 12.sp,
                                fontWeight = if (isCadSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCadSelected) Color.White else Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // HERO MEDIA CANVAS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(14.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            if (!isBlueprintView) {
                // PHOTOREALISTIC STUDIO RENDER
                val successResult = aiResult as? GeminiImageService.GenerationResult.Success
                val bitmap = successResult?.bitmap
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Final Product Render",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    val imageFile = successResult?.file
                    if (imageFile != null && imageFile.exists()) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = "Final Product Render",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Fallback to accurate live compositor preview
                        JigLiveCanvasPreview(config = config, activeStep = 10, showControls = false)
                    }
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
                shape = RoundedCornerShape(6.dp),
                color = Color(0xDD0F172A)
            ) {
                Text(
                    text = "7HOOKS PRECISION TACKLE",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    color = Color(0xFF38BDF8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // GENERATION STATUS BANNER
        val successResult = aiResult as? GeminiImageService.GenerationResult.Success
        if (successResult != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (successResult.isAiGenerated) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (successResult.isAiGenerated) Color(0xFF86EFAC) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier.fillMaxWidth().testTag("ai_status_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (successResult.isAiGenerated) Icons.Default.AutoAwesome else Icons.Default.PrecisionManufacturing,
                        contentDescription = null,
                        tint = if (successResult.isAiGenerated) Color(0xFF16A34A) else Color(0xFF0E3A68),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (successResult.isAiGenerated) "PHOTOREALISTIC STUDIO RENDER" else "PRECISION CAD STUDIO GENERATION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (successResult.isAiGenerated) Color(0xFF16A34A) else Color(0xFF0E3A68)
                        )
                        Text(
                            text = successResult.statusNote.ifBlank { "High-resolution studio product photography" },
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Ref: ${productConfig.referenceNumber} • Model: ${productConfig.modelNumber}",
                    fontSize = 11.5.sp,
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

        // SURFACE STYLING & LIGHTING MODIFIER
        Card(
            modifier = Modifier.fillMaxWidth().testTag("studio_modifier_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF0E3A68),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "JIG STYLING & FINISH MODIFIER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0E3A68)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "CAD LOCKED",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0E3A68)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0F9FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF0E3A68),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Fixed CAD Silhouette: Prompting applies surface finishes, marine water caustics, and studio lighting to your configured ${template.shapeName}.",
                            fontSize = 11.sp,
                            color = Color(0xFF0369A1),
                            lineHeight = 15.sp
                        )
                    }
                }

                Text(
                    text = "Refine surface reflections, iridescence, or water environment for this jig:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                val quickPrompts = listOf(
                    "Underwater sun rays & reef caustics",
                    "Dynamic saltwater spray & wake",
                    "Matte stealth studio lighting",
                    "Holographic laser prism shimmer",
                    "Night glow phosphorescence",
                    "Realistic wet fish scale iridescence"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPrompts.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { promptText = suggestion },
                            label = { Text(suggestion, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholder = { Text("e.g. Add subtle saltwater spray and deep ocean sun rays...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("ai_prompt_text_field"),
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        if (promptText.isNotBlank()) {
                            IconButton(onClick = { promptText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                )

                Button(
                    onClick = {
                        onGenerateWithPrompt(promptText)
                    },
                    enabled = !isGeneratingAi,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("apply_ai_prompt_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E3A68))
                ) {
                    if (isGeneratingAi) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rendering Studio Model...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply Styling to Jig", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // PRIMARY ACTION: TECHNICAL SPECIFICATIONS & CAD
        Button(
            onClick = onOpenEngineering,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("open_technical_specifications_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E3A68))
        ) {
            Icon(
                imageVector = Icons.Default.Architecture,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Technical Specifications & CAD",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // PDF EXPORT ROW: SAVE TO STORAGE / DRIVE & SHARE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSavePdfToStorage,
                enabled = !isGeneratingPdf,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("save_pdf_storage_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
            ) {
                if (isGeneratingPdf) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Save PDF",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onSharePdf,
                enabled = !isGeneratingPdf,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("share_pdf_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E3A68))
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Share PDF",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // SECONDARY ACTIONS: SAVE PROJECT & RE-GENERATE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onSave,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("save_project_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0E3A68))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Save Project",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0E3A68),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = onRegenerate,
                enabled = !isGeneratingAi,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("regenerate_render_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0E3A68))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Re-Render",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0E3A68),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // EDIT CONFIGURATION LINK
        TextButton(
            onClick = onEdit,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF64748B))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit Configuration", color = Color(0xFF64748B), fontSize = 12.5.sp)
        }
    }
}
