package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.viewmodel.ConfiguratorViewModel

@Composable
fun RodEngineeringScreen(
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
                title = "Technical Drawing",
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
                            testTag = "rod_engineering_edit_button"
                        )

                        TactileButton(
                            onClick = { configViewModel.saveCurrentConfig() },
                            modifier = Modifier.weight(1f),
                            variant = TactileButtonVariant.SECONDARY,
                            icon = Icons.Default.BookmarkBorder,
                            text = "Save Blueprint",
                            testTag = "rod_engineering_save_button"
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
                            testTag = "rod_engineering_generate_pdf_button"
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
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Technical Drawing PDF"))
                                },
                                variant = TactileButtonVariant.SUCCESS,
                                icon = Icons.Default.Share,
                                text = "Share",
                                testTag = "rod_engineering_share_pdf_button"
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier.testTag("rod_engineering_screen")
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

            // STEP 3 OF 3 Guided Progress Indicator
            TactileStepIndicator(
                currentStep = 3,
                totalSteps = 3,
                stepTitles = listOf("Choose Rod", "Technical Specs", "Technical Drawing"),
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            // Title block header
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
                            text = "TECHNICAL SPECIFICATION BLUEPRINT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ref: ${currentConfig.referenceNumber} • Graduated Guide Layout",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "A4 COMPLIANT",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // PRIMARY CENTER CAD DRAWING
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "BLANK ARCHITECTURE & GUIDE PLACEMENT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RodEngineeringCanvas(config = currentConfig)
                }
            }

            // PRODUCT SPECIFICATION IDENTIFICATION
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Rod Title Block & Architecture",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    SpecRow(label = "Rod Discipline", value = "${currentConfig.rodType} Rod")
                    SpecRow(label = "Model Number", value = currentConfig.modelNumber, isMonospace = true)
                    SpecRow(label = "Reference Code", value = currentConfig.referenceNumber, isMonospace = true)
                    SpecRow(label = "Piece Sections", value = "${currentConfig.sections} Pieces (Spigot Jointed)")
                    SpecRow(label = "Power / Action Rating", value = "${currentConfig.power} / ${currentConfig.action}")
                }
            }

            // TECHNICAL SPECIFICATIONS TABLE
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Dimensional & Structural Parameters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    SpecRow(label = "Total Length", value = "${currentConfig.lengthMm.toInt()} mm (${String.format(java.util.Locale.US, "%.2f", currentConfig.lengthMm / 1000f)} m)", isBold = true)
                    SpecRow(label = "Finished Rod Mass", value = "${currentConfig.weightGrams.toInt()} g", isBold = true)
                    SpecRow(label = "Max Structural Deadlift", value = "${String.format(java.util.Locale.US, "%.1f", currentConfig.maximumLoadKg)} kg", isBold = true)
                    SpecRow(label = "Handle / Grip Length", value = "${currentConfig.handleLengthMm.toInt()} mm")
                    SpecRow(label = "Blank Core Material", value = currentConfig.material)
                    SpecRow(label = "Guide Hardware System", value = "Fuji K-Series Titanium / SiC Ring Guides (7+1 Layout)")
                    SpecRow(label = "Reel Seat Assembly", value = "Ergonomic Custom Carbon-Touch Locking Reel Seat")
                    SpecRow(label = "Recommended Line Rating", value = currentConfig.recommendedLineWeight)
                    SpecRow(label = "Recommended Lure Rating", value = currentConfig.recommendedLureWeight)
                }
            }

            // MANDATORY DISCLAIMER
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp,
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ENGINEERING DISCLAIMER",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Illustrative load visualization. Actual performance depends on material, construction, manufacturing tolerances and test conditions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
