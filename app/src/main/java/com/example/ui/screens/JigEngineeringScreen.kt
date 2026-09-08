package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.geometry.JigGeometryEngine
import com.example.data.model.ProductCatalog
import com.example.ui.components.*
import com.example.viewmodel.ConfiguratorViewModel

@Composable
fun JigEngineeringScreen(
    configViewModel: ConfiguratorViewModel,
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentConfig by configViewModel.currentConfig.collectAsState()
    val isGeneratingPdf by configViewModel.isGeneratingPdf.collectAsState()
    val pdfValidationResult by configViewModel.pdfValidationResult.collectAsState()
    val saveStatusMessage by configViewModel.saveStatusMessage.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedPerspective by remember { mutableStateOf(JigGeometryEngine.EngineeringPerspective.ORTHOGRAPHIC) }
    var selectedTheme by remember { mutableStateOf(JigGeometryEngine.EngineeringTheme.TECHNICAL_PAPER) }
    var showDimensions by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }

    // Retrieve exact catalog jig product matching selection for true product image
    val matchedJig = remember(currentConfig.productId) {
        ProductCatalog.jigs.find { it.id == currentConfig.productId } ?: ProductCatalog.jigs.first()
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
                snackbarHostState.showSnackbar("A4 PDF Generated: ${result.file.name}")
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
                            text = "Save Blueprint",
                            testTag = "engineering_save_button"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TactileButton(
                            onClick = { configViewModel.generatePdf(context) },
                            enabled = !isGeneratingPdf,
                            modifier = Modifier.weight(1f),
                            variant = TactileButtonVariant.PRIMARY,
                            icon = if (isGeneratingPdf) null else Icons.Default.PictureAsPdf,
                            text = if (isGeneratingPdf) "Generating..." else "Export A4 PDF",
                            testTag = "engineering_generate_pdf_button"
                        )

                        if (pdfValidationResult?.fileUri != null) {
                            TactileButton(
                                onClick = {
                                    val uri = pdfValidationResult?.fileUri ?: return@TactileButton
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Technical Specification PDF"))
                                },
                                variant = TactileButtonVariant.SUCCESS,
                                icon = Icons.Default.Share,
                                text = "Share",
                                testTag = "engineering_share_pdf_button"
                            )
                        }
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

            // SECTION 13: DEDICATED PRODUCT REFERENCE AREA WITH EXACT SELECTED JIG
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
                            text = "PRODUCT REFERENCE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = matchedJig.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Controlled, medium-sized image preview of the exact selected jig
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = matchedJig.localDrawableRes ?: matchedJig.imageUrl,
                            contentDescription = "Selected Jig: ${matchedJig.name}",
                            contentScale = ContentScale.Fit,
                            error = painterResource(id = com.example.R.drawable.jig_orange_black_real),
                            fallback = painterResource(id = com.example.R.drawable.jig_orange_black_real),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Finish: ${currentConfig.colorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Thread: ${currentConfig.threadColor}",
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
                    // Title Bar with Dimension and Grid Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DYNAMIC CAD & TECHNICAL DRAWING",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "First-angle orthographic projection & parametric geometry",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = showDimensions,
                                onClick = { showDimensions = !showDimensions },
                                label = { Text("Dims", fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (showDimensions) Icons.Default.Straighten else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            FilterChip(
                                selected = showGrid,
                                onClick = { showGrid = !showGrid },
                                label = { Text("Grid", fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (showGrid) Icons.Default.GridOn else Icons.Default.GridOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
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
