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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.pdf.PdfGenerator
import com.example.ui.components.*
import com.example.viewmodel.ConfiguratorViewModel

@Composable
fun LureEngineeringScreen(
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
                            testTag = "lure_engineering_edit_button"
                        )

                        TactileButton(
                            onClick = { configViewModel.saveCurrentConfig() },
                            modifier = Modifier.weight(1f),
                            variant = TactileButtonVariant.SECONDARY,
                            icon = Icons.Default.BookmarkBorder,
                            text = "Save Blueprint",
                            testTag = "lure_engineering_save_button"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TactileButton(
                            onClick = { configViewModel.generatePdf(context) },
                            modifier = Modifier.weight(1f),
                            variant = TactileButtonVariant.PRIMARY,
                            icon = if (isGeneratingPdf) null else Icons.Default.PictureAsPdf,
                            text = if (isGeneratingPdf) "Exporting..." else "Export A4 PDF",
                            enabled = !isGeneratingPdf,
                            testTag = "lure_engineering_generate_pdf_button"
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
                                modifier = Modifier.weight(1f),
                                variant = TactileButtonVariant.SUCCESS,
                                icon = Icons.Default.Share,
                                text = "Share",
                                testTag = "lure_engineering_share_pdf_button"
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier.testTag("lure_engineering_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // STEP 3 OF 3 Guided Progress Indicator
            TactileStepIndicator(
                currentStep = 3,
                totalSteps = 3,
                stepTitles = listOf("Choose Lure", "Hydrodynamics", "Technical Drawing"),
                modifier = Modifier.padding(horizontal = 0.dp)
            )

            // Document Reference Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TECHNICAL DRAWING SPECIFICATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${currentConfig.productName} • ${currentConfig.modelNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = currentConfig.referenceNumber,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // High-precision Industrial CAD Canvas
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ORTHOGRAPHIC PROJECTION (1:1 CAD)",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "ANSI Y14.5 COMPLIANT",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LureEngineeringCanvas(
                        config = currentConfig,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    )
                }
            }

            // PDF Ready Banner if generated
            pdfValidationResult?.let { result ->
                if (result.isValid) {
                    TactileCard(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 2.dp,
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "A4 PDF Ready for Production",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${result.file.name} (${result.fileSize / 1024} KB)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            TactileButton(
                                onClick = {
                                    val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(result.fileUri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    try {
                                        context.startActivity(Intent.createChooser(openIntent, "Open PDF"))
                                    } catch (_: Exception) {}
                                },
                                variant = TactileButtonVariant.PRIMARY,
                                icon = Icons.Default.Visibility,
                                text = "Open",
                                testTag = "open_pdf_button"
                            )
                        }
                    }
                }
            }

            // Engineering Data Table
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MANUFACTURING SPECIFICATION DATA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val specs = listOf(
                        "Product Name" to currentConfig.productName,
                        "Model Number" to currentConfig.modelNumber,
                        "Lure Platform" to currentConfig.category,
                        "Target Mass" to "${String.format(java.util.Locale.US, "%.1f", currentConfig.weightGrams)} grams",
                        "Overall Length" to "${currentConfig.lengthMm.toInt()} mm",
                        "Maximum Body Width" to "${currentConfig.widthMm.toInt()} mm",
                        "Lip Diving Depth" to "${String.format(java.util.Locale.US, "%.1f", currentConfig.divingDepthMeters)} meters",
                        "Buoyancy Dynamic" to currentConfig.buoyancy,
                        "Terminal Hooks" to currentConfig.hookType,
                        "Body Composition" to currentConfig.material,
                        "Finish Application" to currentConfig.colorName
                    )

                    specs.forEachIndexed { idx, (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (idx < specs.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Hydrodynamic Tolerances Card
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FACTORY QUALITY & TOLERANCES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Injection Molded ABS Shell: ±0.15 mm wall thickness\n" +
                               "• Internal Tungsten Ball Transfer: 100% magnetic chamber lock\n" +
                               "• Ultra-Sonic Body Welded: Hydrostatic test to 5.0 ATM\n" +
                               "• Stainless Steel Through-Wire: 304 marine grade loop-tested to 60 kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
