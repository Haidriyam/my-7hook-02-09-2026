package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PackagingType
import com.example.ui.components.*
import com.example.viewmodel.PackagingViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PackagingScreen(
    packagingViewModel: PackagingViewModel,
    onNavigateBack: () -> Unit
) {
    val packagingConfig by packagingViewModel.packagingConfig.collectAsState()
    val isGeneratingPdf by packagingViewModel.isGeneratingPdf.collectAsState()
    val pdfValidationResult by packagingViewModel.pdfValidationResult.collectAsState()
    val saveStatusMessage by packagingViewModel.saveStatusMessage.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Zero-permission Photo Picker for distributor logo (Google Play compliant)
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            packagingViewModel.updateCustomLogoUri(uri.toString())
        }
    }

    LaunchedEffect(saveStatusMessage) {
        saveStatusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            packagingViewModel.clearStatusMessage()
        }
    }

    LaunchedEffect(pdfValidationResult) {
        pdfValidationResult?.let { result ->
            if (result.isValid) {
                snackbarHostState.showSnackbar("Packaging Specification PDF Created: ${result.file.name}")
            }
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Packaging Configuration",
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
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TactileButton(
                            onClick = { packagingViewModel.savePackagingConfig() },
                            modifier = Modifier.weight(1f),
                            variant = TactileButtonVariant.SECONDARY,
                            icon = Icons.Default.BookmarkBorder,
                            text = "Save Package",
                            testTag = "packaging_save_button"
                        )

                        TactileButton(
                            onClick = { packagingViewModel.generatePackagingPdf(context) },
                            enabled = !isGeneratingPdf,
                            modifier = Modifier.weight(1f),
                            variant = TactileButtonVariant.PRIMARY,
                            icon = if (isGeneratingPdf) null else Icons.Default.PictureAsPdf,
                            text = if (isGeneratingPdf) "Generating..." else "Export PDF",
                            testTag = "packaging_generate_pdf_button"
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
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Packaging Specification"))
                                },
                                variant = TactileButtonVariant.SUCCESS,
                                icon = Icons.Default.Share,
                                text = "Share",
                                testTag = "packaging_share_pdf_button"
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier.testTag("packaging_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // STEP 1: PACKAGING TYPE SELECTION (Section 18)
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
                            text = "STEP 1: PACKAGING TYPE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = packagingConfig.packagingType.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PackagingType.values().forEach { type ->
                            val isSelected = packagingConfig.packagingType.equals(type.displayName, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { packagingViewModel.updatePackagingType(type.displayName) },
                                label = { Text(type.displayName, fontSize = 11.5.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // STEP 2: BRAND & LOGO SETUP (Section 17 & 19)
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
                    Text(
                        text = "STEP 2: BRAND & LOGO SETUP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (packagingConfig.customLogoUri != null) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = Uri.parse(packagingConfig.customLogoUri),
                                    contentDescription = "Distributor Logo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize().padding(4.dp)
                                )
                            }

                            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TactileButton(
                                    onClick = { logoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    modifier = Modifier.weight(1f),
                                    variant = TactileButtonVariant.PRIMARY,
                                    icon = Icons.Default.CloudUpload,
                                    text = "Replace"
                                )
                                TactileButton(
                                    onClick = { packagingViewModel.updateCustomLogoUri(null) },
                                    modifier = Modifier.weight(1f),
                                    variant = TactileButtonVariant.OUTLINE,
                                    icon = Icons.Default.DeleteOutline,
                                    text = "Remove"
                                )
                            }
                        } else {
                            TactileButton(
                                onClick = { logoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                modifier = Modifier.fillMaxWidth().testTag("upload_logo_button"),
                                variant = TactileButtonVariant.OUTLINE,
                                icon = Icons.Default.AddPhotoAlternate,
                                text = "Upload Distributor Logo (PNG / JPG)"
                            )
                        }
                    }
                }
            }

            // STEP 3: PACKAGING DETAILS (Mobile Responsive Compact Form)
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
                    Text(
                        text = "STEP 3: PACKAGING DETAILS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = packagingConfig.companyName,
                            onValueChange = { packagingViewModel.updateCompanyName(it) },
                            label = { Text("Brand / Company *", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("packaging_company_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )
                        OutlinedTextField(
                            value = packagingConfig.modelNumber,
                            onValueChange = { packagingViewModel.updateModelNumber(it) },
                            label = { Text("Model SKU *", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("packaging_model_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = packagingConfig.productName,
                            onValueChange = { packagingViewModel.updateProductName(it) },
                            label = { Text("Product Line *", fontSize = 11.sp) },
                            modifier = Modifier.weight(1.3f).testTag("packaging_product_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )
                        OutlinedTextField(
                            value = packagingConfig.targetQuantity,
                            onValueChange = { packagingViewModel.updateDetails(quantity = it) },
                            label = { Text("Order Qty", fontSize = 11.sp) },
                            modifier = Modifier.weight(0.7f),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
            }

            // STEP 4: COLOR & APPEARANCE (Cardstock, Hanging Slot, Window, Finish)
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
                    Text(
                        text = "STEP 4: COLOR & APPEARANCE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = packagingConfig.packagingDimensions,
                            onValueChange = { packagingViewModel.updateDetails(dimensions = it) },
                            label = { Text("Dimensions (H x W x D)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )
                        OutlinedTextField(
                            value = packagingConfig.cardStock,
                            onValueChange = { packagingViewModel.updateDetails(cardStock = it) },
                            label = { Text("Cardstock / Material", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = packagingConfig.windowStyle,
                            onValueChange = { packagingViewModel.updateDetails(windowStyle = it) },
                            label = { Text("Window Cutout Style", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )
                        OutlinedTextField(
                            value = packagingConfig.hangingSlot,
                            onValueChange = { packagingViewModel.updateDetails(hangingSlot = it) },
                            label = { Text("Hanger Hole Format", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )
                    }

                    OutlinedTextField(
                        value = packagingConfig.printingProcess,
                        onValueChange = { packagingViewModel.updateDetails(printingProcess = it) },
                        label = { Text("Printing & Surface Finish (e.g. 6-Color Offset + Spot UV)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            }

            // STEP 5: PREVIEW (Controlled Perspective Retail Presentation)
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "STEP 5: RETAIL MOCKUP PREVIEW",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PackagingPreviewMockup(config = packagingConfig)
                }
            }

            // STEP 6: PACKAGING SPECIFICATION SUMMARY TABLE
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "STEP 6: PACKAGING SPECIFICATION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    SpecRow(label = "Format", value = packagingConfig.packagingType, isBold = true)
                    SpecRow(label = "Brand / Client", value = packagingConfig.companyName)
                    SpecRow(label = "Product Line", value = packagingConfig.productName)
                    SpecRow(label = "Model SKU", value = packagingConfig.modelNumber, isMonospace = true)
                    SpecRow(label = "Dimensions", value = packagingConfig.packagingDimensions, isBold = true)
                    SpecRow(label = "Material", value = packagingConfig.cardStock)
                    SpecRow(label = "Window Cutout", value = packagingConfig.windowStyle)
                    SpecRow(label = "Hanger Hole", value = packagingConfig.hangingSlot)
                    SpecRow(label = "Print Finish", value = packagingConfig.printingProcess)
                    SpecRow(label = "Production Qty", value = "${packagingConfig.targetQuantity} units", isMonospace = true)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
