package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Technical Drawing PDF"))
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

            // STEP 3 OF 3 Guided Progress Indicator
            TactileStepIndicator(
                currentStep = 3,
                totalSteps = 3,
                stepTitles = listOf("Choose Jig", "Technical Specs", "Technical Drawing"),
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            // Technical Drawing Title Badge
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
                            text = "TECHNICAL DRAWING SPECIFICATION",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ref: ${currentConfig.referenceNumber} • Standard Scale 1:1",
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

            // PRIMARY CENTER CAD DRAWING
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ORTHOGRAPHIC PROJECTION & DIMENSIONS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    JigEngineeringCanvas(config = currentConfig)
                }
            }

            // PRODUCT INFORMATION & TITLE BLOCK
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
                        text = "Title Block & Product Identification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    SpecRow(label = "Product Name", value = currentConfig.productName)
                    SpecRow(label = "Model Number", value = currentConfig.modelNumber, isMonospace = true)
                    SpecRow(label = "Category", value = currentConfig.category)
                    SpecRow(label = "Reference Code", value = currentConfig.referenceNumber, isMonospace = true)
                    SpecRow(label = "Drawing Type", value = "Standard Orthographic Technical Projection")
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
                        text = "Dimensional & Physical Parameters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    SpecRow(label = "Total Length", value = "${currentConfig.lengthMm.toInt()} mm (±0.5mm)", isBold = true)
                    SpecRow(label = "Max Body Width", value = "${currentConfig.widthMm.toInt()} mm (±0.3mm)", isBold = true)
                    SpecRow(label = "Finished Lure Mass", value = "${currentConfig.weightGrams.toInt()} g (±1.5g)", isBold = true)
                    SpecRow(label = "Core Construction", value = currentConfig.material)
                    SpecRow(label = "Holographic Finish", value = currentConfig.colorName)
                    SpecRow(label = "Lure Action / Pattern", value = currentConfig.patternType.name.replace("_", " "))
                    SpecRow(label = "Line-Tie Eyelets", value = "Dual SUS304 Welded Solid Rings")
                    SpecRow(label = "Surface Coating", value = "Multi-Layer Anti-Saltwater Epoxy Clear Coat")
                }
            }

            // MANUFACTURING TOLERANCE & QUALITY ASSURANCE
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
                                "2. Center of gravity is calculated for vertical flutter descent.\n" +
                                "3. Salt-spray corrosion test rating: 500 hours minimum per 7Hooks QA standards.",
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
