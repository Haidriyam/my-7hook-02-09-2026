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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

    // Zero-permission Photo Picker for custom logo (Google Play policy compliant)
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
                snackbarHostState.showSnackbar("A4 Packaging PDF Generated: ${result.file.name}")
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
                        .padding(12.dp),
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
                            text = if (isGeneratingPdf) "Generating..." else "Generate PDF",
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
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Packaging Specification PDF"))
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
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Subheader Card
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Custom Packaging & Brand Customizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upload distributor logos, configure box/pouch dimensions, barcode details, and generate print-ready A4 die-cut specification sheets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            // 2.5D PERSPECTIVE MOCKUP PREVIEW
            Text(
                text = "2.5D PACKAGING MOCKUP PREVIEW",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )

            PackagingPreviewMockup(config = packagingConfig)

            // COMPANY LOGO UPLOAD SECTION
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
                        text = "Distributor / Company Logo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (packagingConfig.customLogoUri != null) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = Uri.parse(packagingConfig.customLogoUri),
                                    contentDescription = "Uploaded Logo Preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                TactileButton(
                                    onClick = { logoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    variant = TactileButtonVariant.PRIMARY,
                                    icon = Icons.Default.CloudUpload,
                                    text = "Change Logo"
                                )
                                TactileButton(
                                    onClick = { packagingViewModel.updateCustomLogoUri(null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    variant = TactileButtonVariant.OUTLINE,
                                    icon = Icons.Default.DeleteOutline,
                                    text = "Remove Logo"
                                )
                            }
                        } else {
                            TactileButton(
                                onClick = { logoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("upload_logo_button"),
                                variant = TactileButtonVariant.OUTLINE,
                                icon = Icons.Default.AddPhotoAlternate,
                                text = "Upload Company Logo (PNG / JPG)"
                            )
                        }
                    }
                }
            }

            // PACKAGING TYPE SELECTION
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Packaging Enclosure Style",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PackagingType.values().forEach { type ->
                            val isSelected = packagingConfig.packagingType.equals(type.displayName, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { packagingViewModel.updatePackagingType(type.displayName) },
                                label = { Text(type.displayName) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // SPECIFICATION FIELDS
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
                        text = "Branding & Contact Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = packagingConfig.companyName,
                        onValueChange = { packagingViewModel.updateCompanyName(it) },
                        label = { Text("Company / Distributor Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("packaging_company_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = packagingConfig.productName,
                        onValueChange = { packagingViewModel.updateProductName(it) },
                        label = { Text("Product / Line Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("packaging_product_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = packagingConfig.modelNumber,
                        onValueChange = { packagingViewModel.updateModelNumber(it) },
                        label = { Text("Model Number / SKU *") },
                        modifier = Modifier.fillMaxWidth().testTag("packaging_model_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = packagingConfig.packagingDimensions,
                        onValueChange = { packagingViewModel.updateDetails(dimensions = it) },
                        label = { Text("Dimensions (e.g. 180 x 65 x 25 mm)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = packagingConfig.packagingMaterial,
                        onValueChange = { packagingViewModel.updateDetails(material = it) },
                        label = { Text("Material (e.g. 350gsm Kraft Cardstock / PVC Blister)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = packagingConfig.contactWebsite,
                        onValueChange = { packagingViewModel.updateDetails(website = it) },
                        label = { Text("Website") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = packagingConfig.contactEmail,
                        onValueChange = { packagingViewModel.updateDetails(email = it) },
                        label = { Text("Contact Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = packagingConfig.productDescription,
                        onValueChange = { packagingViewModel.updateDetails(description = it) },
                        label = { Text("Product Description / Back Copy") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = packagingConfig.packagingNotes,
                        onValueChange = { packagingViewModel.updateDetails(notes = it) },
                        label = { Text("Die-Cut / Print Finishing Notes (Foil, Spot UV, Euro-Slot)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
