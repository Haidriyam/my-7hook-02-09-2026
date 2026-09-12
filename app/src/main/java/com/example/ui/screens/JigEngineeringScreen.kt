package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ai.GeminiImageService
import com.example.data.geometry.JigGeometryEngine
import com.example.data.model.ProductCatalog
import com.example.data.model.JigShapeRepository
import com.example.ui.components.*
import com.example.viewmodel.ConfiguratorViewModel
import kotlinx.coroutines.launch

@Composable
fun JigEngineeringScreen(
    configViewModel: ConfiguratorViewModel,
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentConfig by configViewModel.currentConfig.collectAsState()
    val currentJigConfig by configViewModel.currentJigConfig.collectAsState()
    val isGeneratingPdf by configViewModel.isGeneratingPdf.collectAsState()
    val pdfValidationResult by configViewModel.pdfValidationResult.collectAsState()
    val saveStatusMessage by configViewModel.saveStatusMessage.collectAsState()
    val aiResult by configViewModel.aiGenerationResult.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedPerspective by remember { mutableStateOf(JigGeometryEngine.EngineeringPerspective.ORTHOGRAPHIC) }
    var selectedTheme by remember { mutableStateOf(JigGeometryEngine.EngineeringTheme.TECHNICAL_PAPER) }
    var showDimensions by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }

    var pendingSavePdfToStorage by remember { mutableStateOf(false) }
    var pendingSharePdf by remember { mutableStateOf(false) }

    val template = remember(currentJigConfig.shapeId) {
        JigShapeRepository.getById(currentJigConfig.shapeId)
    }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            val file = pdfValidationResult?.file
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
        val file = pdfValidationResult?.file
        if (file != null && file.exists()) {
            val fileUri = pdfValidationResult?.fileUri ?: androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "7Hooks Technical Specification - ${currentConfig.productName}")
                putExtra(Intent.EXTRA_TEXT, "7Hooks Precision Tackle Engineering Blueprint for ${currentConfig.productName} (${currentConfig.weightGrams.toInt()}g).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Technical Specification PDF"))
        }
    }

    LaunchedEffect(saveStatusMessage) {
        saveStatusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            configViewModel.clearStatusMessage()
        }
    }

    LaunchedEffect(pdfValidationResult) {
        pdfValidationResult?.let { result ->
            if (result.isValid) {
                if (pendingSavePdfToStorage) {
                    pendingSavePdfToStorage = false
                    val sanitized = currentConfig.productName.replace(" ", "_")
                    savePdfLauncher.launch("7Hooks_${sanitized}_${currentConfig.weightGrams.toInt()}g_Blueprint.pdf")
                } else if (pendingSharePdf) {
                    pendingSharePdf = false
                    launchSharePdf()
                } else {
                    snackbarHostState.showSnackbar("A4 PDF Generated: ${result.file.name}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Technical Specification",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TactileButton(
                            onClick = onNavigateToEdit,
                            modifier = Modifier.weight(1f),
                            variant = TactileButtonVariant.OUTLINE,
                            icon = Icons.Default.Edit,
                            text = "Edit Specs",
                            testTag = "engineering_edit_button"
                        )

                        TactileButton(
                            onClick = { configViewModel.saveCurrentConfig() },
                            modifier = Modifier.weight(1f),
                            variant = TactileButtonVariant.SECONDARY,
                            icon = Icons.Default.BookmarkBorder,
                            text = "Save Project",
                            testTag = "engineering_save_button"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TactileButton(
                            onClick = {
                                if (pdfValidationResult?.file?.exists() == true) {
                                    val sanitized = currentConfig.productName.replace(" ", "_")
                                    savePdfLauncher.launch("7Hooks_${sanitized}_${currentConfig.weightGrams.toInt()}g_Blueprint.pdf")
                                } else {
                                    pendingSavePdfToStorage = true
                                    configViewModel.generatePdf(context)
                                }
                            },
                            enabled = !isGeneratingPdf,
                            modifier = Modifier.weight(1f),
                            variant = TactileButtonVariant.PRIMARY,
                            icon = if (isGeneratingPdf) null else Icons.Default.SaveAlt,
                            text = if (isGeneratingPdf) "Generating..." else "Save PDF",
                            testTag = "engineering_save_pdf_storage_button"
                        )

                        TactileButton(
                            onClick = {
                                if (pdfValidationResult?.file?.exists() == true) {
                                    launchSharePdf()
                                } else {
                                    pendingSharePdf = true
                                    configViewModel.generatePdf(context)
                                }
                            },
                            enabled = !isGeneratingPdf,
                            modifier = Modifier.weight(1f),
                            variant = TactileButtonVariant.SUCCESS,
                            icon = Icons.Default.Share,
                            text = "Share PDF",
                            testTag = "engineering_share_pdf_button"
                        )
                    }
                }
            }
        },
        modifier = Modifier.testTag("jig_engineering_screen")
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

            // Guided Progress Indicator
            TactileStepIndicator(
                currentStep = 3,
                totalSteps = 3,
                stepTitles = listOf("Choose Jig", "Configure Specs", "Technical Spec & Drawing"),
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            // Technical Specification Header Badge
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
                            text = "TECHNICAL SPECIFICATION",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ref: ${currentConfig.referenceNumber} • Engineering Drawing Standard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "A4 COMPLIANT",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // SECTION 13: DEDICATED PRODUCT REFERENCE AREA WITH EXACT CUSTOMER-DESIGNED JIG
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DESIGNED PRODUCT REFERENCE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${template.shapeName} • ${currentConfig.weightGrams.toInt()}g",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Controlled preview displaying the EXACT file / live model the customer configured
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val successResult = aiResult as? GeminiImageService.GenerationResult.Success
                        val bitmap = successResult?.bitmap
                        val imageFile = successResult?.file

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Configured ${template.shapeName}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        } else if (imageFile != null && imageFile.exists()) {
                            AsyncImage(
                                model = imageFile,
                                contentDescription = "Configured ${template.shapeName}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        } else {
                            JigLiveCanvasPreview(
                                config = currentJigConfig,
                                activeStep = 10,
                                showControls = false
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Finish: ${currentConfig.finishType} • Pattern: ${currentConfig.patternType.name.replace('_', ' ')}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Rigging: ${currentConfig.hookTypeJig}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // SECTION 14 & 15: SPECIFICATION TABLE
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Engineering Specifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    SpecRow(label = "Product Name", value = currentConfig.productName, isBold = true)
                    SpecRow(label = "Model Number", value = currentConfig.modelNumber, isMonospace = true)
                    SpecRow(label = "Category", value = currentConfig.category)
                    SpecRow(label = "Target Weight", value = "${currentConfig.weightGrams.toInt()} g", isBold = true)
                    SpecRow(label = "Overall Length", value = "${currentConfig.lengthMm.toInt()} mm", isBold = true)
                    SpecRow(label = "Max Body Width", value = "${currentConfig.widthMm.toInt()} mm", isBold = true)
                    SpecRow(label = "Core Construction", value = currentConfig.material)
                    SpecRow(label = "Primary Color", value = currentConfig.colorName)
                    SpecRow(label = "Surface Finish", value = currentConfig.finishType)
                    SpecRow(label = "Pattern Type", value = currentConfig.patternType.name.replace("_", " "))
                    SpecRow(label = "Strike Eye", value = currentConfig.eyeStyle)
                    SpecRow(label = "Assist Hook", value = currentConfig.hookTypeJig)
                    SpecRow(
                        label = "Thread",
                        value = if (currentConfig.threadColor == "None") "None" else "${currentConfig.threadColor} (${currentConfig.threadWrapping})",
                        isBold = currentConfig.threadColor != "None"
                    )
                    SpecRow(label = "Line-Tie Eyelets", value = "Dual SUS304 Welded Solid Rings")
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                    
                    // Tolerances block per Section 33 (No fake tolerances)
                    SpecRow(label = "General Tolerance", value = "TBD", isMonospace = true)
                    SpecRow(label = "Weight Tolerance", value = "TBD", isMonospace = true)
                    SpecRow(label = "Dimensional Tolerance", value = "TBD", isMonospace = true)
                }
            }

            // PRIMARY ORTHOGRAPHIC TECHNICAL DRAWING SCHEMATIC & DYNAMIC STUDIO
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Title Bar with Dimension and Grid Toggles - Staged vertically to prevent overflow
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "DYNAMIC CAD & TECHNICAL DRAWING",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "First-angle orthographic projection & parametric geometry of your configured jig",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )

                        // Dedicated, clean toggle controls row with generous spacing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = showDimensions,
                                onClick = { showDimensions = !showDimensions },
                                label = {
                                    Text(
                                        text = if (showDimensions) "Dimensions: ON" else "Dimensions: OFF",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (showDimensions) Icons.Default.Straighten else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = showGrid,
                                onClick = { showGrid = !showGrid },
                                label = {
                                    Text(
                                        text = if (showGrid) "Grid: ON" else "Grid: OFF",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (showGrid) Icons.Default.GridOn else Icons.Default.GridOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Main CAD Canvas
                    JigEngineeringCanvas(
                        config = currentConfig,
                        perspective = selectedPerspective,
                        theme = selectedTheme,
                        showDimensions = showDimensions,
                        showGrid = showGrid,
                        modifier = Modifier.height(360.dp)
                    )

                    // 1. Perspective View Options
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "VISUAL PERSPECTIVE VIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.4.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            JigGeometryEngine.EngineeringPerspective.values().forEach { persp ->
                                val isSelected = selectedPerspective == persp
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedPerspective = persp },
                                    label = { Text(persp.displayName, fontSize = 10.5.sp) },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null
                                )
                            }
                        }
                    }

                    // 2. CAD Drafting Theme Options
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "DRAFTING & CAD THEME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.4.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            JigGeometryEngine.EngineeringTheme.values().forEach { th ->
                                val isSelected = selectedTheme == th
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTheme = th },
                                    label = { Text(th.displayName, fontSize = 10.5.sp) },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            // QUALITY ASSURANCE & FACTORY NOTES
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Engineering & Quality Assurance Notes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "1. Dimensions are specified in millimeters (mm) at standard temperature and pressure.\n" +
                                "2. Center of gravity calculated for vertical flutter descent.\n" +
                                "3. Salt-spray corrosion test: 500 hours minimum per 7Hooks QA marine standards.\n" +
                                "4. Assist hook rigged with high-tensile braided PE cord and whip-finished thread binding.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SpecRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.44f),
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(0.56f),
            fontSize = 12.sp
        )
    }
}
