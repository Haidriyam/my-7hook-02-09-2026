package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.ConfiguratorViewModel
import com.example.viewmodel.PackagingViewModel

enum class ViewportPreset(val title: String, val widthDp: Dp?, val heightDp: Dp?, val label: String) {
    AUTO("Fit Device", null, null, "Native"),
    SMALL_PHONE("Small Phone", 360.dp, 800.dp, "360×800"),
    NORMAL_PHONE("Normal Phone", 390.dp, 844.dp, "390×844"),
    LARGE_PHONE("Large Phone", 430.dp, 932.dp, "430×932"),
    TABLET("Tablet", 800.dp, 1280.dp, "800×1280"),
    DESKTOP("Desktop Preview", 1280.dp, 800.dp, "1280×800")
}

enum class QaScreenDestination(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    LOGIN("Login", Icons.Default.Lock),
    SIGNUP("Signup", Icons.Default.PersonAdd),
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    JIG_CATALOG("Jigs Catalog", Icons.Default.ViewList),
    JIG_CONFIG("Jig Config", Icons.Default.Tune),
    JIG_ENGINEERING("Jig CAD", Icons.Default.Architecture),
    ROD_CATALOG("Rods Catalog", Icons.Default.Category),
    ROD_CONFIG("Rod Config", Icons.Default.Build),
    ROD_ENGINEERING("Rod CAD & Sim", Icons.Default.Speed),
    PACKAGING("Packaging", Icons.Default.Inventory2),
    TEXTURES_GALLERY("Material Textures", Icons.Default.AutoAwesome),
    PDF_PREVIEW("A4 PDF Preview", Icons.Default.PictureAsPdf)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualQaScreen(
    authViewModel: AuthViewModel,
    configViewModel: ConfiguratorViewModel,
    packagingViewModel: PackagingViewModel,
    onExitQa: () -> Unit
) {
    var selectedViewport by remember { mutableStateOf(ViewportPreset.AUTO) }
    var selectedScreen by remember { mutableStateOf(QaScreenDestination.DASHBOARD) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var showControls by remember { mutableStateOf(true) }

    // Test data toggle for PDF inspection
    var pdfTestWeightGrams by remember { mutableFloatStateOf(50f) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("visual_qa_screen"),
        color = Color(0xFF0F172A)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // QA Studio Top Control Bar
            Surface(
                color = Color(0xFF1E293B),
                tonalElevation = 6.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = onExitQa,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Exit QA Mode",
                                    tint = Color.White
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "VISUAL QA STUDIO",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF38BDF8),
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Surface(
                                        color = Color(0xFF0E3A68),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "DEV MODE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Interactive Screen & Viewport Inspector",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Zoom buttons
                            IconButton(
                                onClick = { zoomScale = (zoomScale - 0.15f).coerceAtLeast(0.4f) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "${(zoomScale * 100).toInt()}%",
                                fontSize = 10.sp,
                                color = Color(0xFFCBD5E1),
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(
                                onClick = { zoomScale = (zoomScale + 0.15f).coerceAtMost(1.5f) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { zoomScale = 1f },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = "Reset Zoom", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            }

                            Button(
                                onClick = onExitQa,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Exit QA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Viewport Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Viewport: ", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        ViewportPreset.values().forEach { preset ->
                            FilterChip(
                                selected = selectedViewport == preset,
                                onClick = {
                                    selectedViewport = preset
                                    if (preset == ViewportPreset.TABLET || preset == ViewportPreset.DESKTOP) {
                                        zoomScale = 0.65f
                                    } else {
                                        zoomScale = 1f
                                    }
                                },
                                label = {
                                    Text(
                                        text = "${preset.title} (${preset.label})",
                                        fontSize = 11.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0E3A68),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF334155),
                                    labelColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Screen Selector Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Screen: ", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        QaScreenDestination.values().forEach { dest ->
                            AssistChip(
                                onClick = { selectedScreen = dest },
                                label = { Text(dest.displayName, fontSize = 11.sp, fontWeight = if (selectedScreen == dest) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    Icon(
                                        dest.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (selectedScreen == dest) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (selectedScreen == dest) Color(0xFF0369A1) else Color(0xFF1E293B),
                                    labelColor = if (selectedScreen == dest) Color.White else Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }

            // Canvas Workspace Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0B1120))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer device frame styling
                val targetWidth = selectedViewport.widthDp
                val targetHeight = selectedViewport.heightDp

                Box(
                    modifier = Modifier
                        .scale(zoomScale)
                        .then(
                            if (targetWidth != null && targetHeight != null) {
                                Modifier
                                    .size(targetWidth, targetHeight)
                                    .shadow(16.dp, RoundedCornerShape(20.dp))
                                    .border(3.dp, Color(0xFF475569), RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(18.dp))
                            } else {
                                Modifier.fillMaxSize()
                            }
                        )
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Render selected interactive screen
                    when (selectedScreen) {
                        QaScreenDestination.LOGIN -> {
                            LoginScreen(
                                authViewModel = authViewModel,
                                onLoginSuccess = { selectedScreen = QaScreenDestination.DASHBOARD },
                                onNavigateToSignup = { selectedScreen = QaScreenDestination.SIGNUP }
                            )
                        }
                        QaScreenDestination.SIGNUP -> {
                            SignupScreen(
                                authViewModel = authViewModel,
                                onSignupSuccess = { selectedScreen = QaScreenDestination.DASHBOARD },
                                onNavigateBackToLogin = { selectedScreen = QaScreenDestination.LOGIN }
                            )
                        }
                        QaScreenDestination.DASHBOARD -> {
                            DashboardScreen(
                                currentUser = UserAccount("qa-engineer", "QA Engineer", "engineer@7hooks.com"),
                                configViewModel = configViewModel,
                                onNavigateToJigs = { selectedScreen = QaScreenDestination.JIG_CATALOG },
                                onNavigateToRods = { selectedScreen = QaScreenDestination.ROD_CATALOG },
                                onNavigateToPackaging = { selectedScreen = QaScreenDestination.PACKAGING },
                                onNavigateToEngineering = { entity ->
                                    configViewModel.loadSavedConfig(entity)
                                    if (entity.configType == ConfigType.JIG.name) {
                                        selectedScreen = QaScreenDestination.JIG_ENGINEERING
                                    } else {
                                        selectedScreen = QaScreenDestination.ROD_ENGINEERING
                                    }
                                },
                                onLogout = { selectedScreen = QaScreenDestination.LOGIN }
                            )
                        }
                        QaScreenDestination.JIG_CATALOG -> {
                            JigCatalogScreen(
                                onSelectShape = { selectedShape ->
                                    configViewModel.selectShape(selectedShape)
                                    selectedScreen = QaScreenDestination.JIG_CONFIG
                                },
                                onNavigateBack = { selectedScreen = QaScreenDestination.DASHBOARD }
                            )
                        }
                        QaScreenDestination.JIG_CONFIG -> {
                            JigConfigScreen(
                                configViewModel = configViewModel,
                                onNavigateToEngineering = { selectedScreen = QaScreenDestination.JIG_ENGINEERING },
                                onNavigateBack = { selectedScreen = QaScreenDestination.JIG_CATALOG }
                            )
                        }
                        QaScreenDestination.JIG_ENGINEERING -> {
                            JigEngineeringScreen(
                                configViewModel = configViewModel,
                                onNavigateToEdit = { selectedScreen = QaScreenDestination.JIG_CONFIG },
                                onNavigateBack = { selectedScreen = QaScreenDestination.DASHBOARD }
                            )
                        }
                        QaScreenDestination.ROD_CATALOG -> {
                            RodCategoryScreen(
                                onSelectCategory = { cat, lengthGroup ->
                                    configViewModel.selectRodCategory(cat, lengthGroup)
                                    selectedScreen = QaScreenDestination.ROD_CONFIG
                                },
                                onNavigateBack = { selectedScreen = QaScreenDestination.DASHBOARD }
                            )
                        }
                        QaScreenDestination.ROD_CONFIG -> {
                            RodConfigScreen(
                                configViewModel = configViewModel,
                                onNavigateToEngineering = { selectedScreen = QaScreenDestination.ROD_ENGINEERING },
                                onNavigateBack = { selectedScreen = QaScreenDestination.ROD_CATALOG }
                            )
                        }
                        QaScreenDestination.ROD_ENGINEERING -> {
                            RodEngineeringScreen(
                                configViewModel = configViewModel,
                                onNavigateToEdit = { selectedScreen = QaScreenDestination.ROD_CONFIG },
                                onNavigateBack = { selectedScreen = QaScreenDestination.DASHBOARD }
                            )
                        }
                        QaScreenDestination.PACKAGING -> {
                            PackagingScreen(
                                packagingViewModel = packagingViewModel,
                                onNavigateBack = { selectedScreen = QaScreenDestination.DASHBOARD }
                            )
                        }
                        QaScreenDestination.TEXTURES_GALLERY -> {
                            RealisticTexturesGalleryScreen(
                                onNavigateBack = { selectedScreen = QaScreenDestination.DASHBOARD }
                            )
                        }
                        QaScreenDestination.PDF_PREVIEW -> {
                            A4PdfVisualInspector(
                                configViewModel = configViewModel,
                                testWeightGrams = pdfTestWeightGrams,
                                onToggleWeight = {
                                    pdfTestWeightGrams = if (pdfTestWeightGrams == 50f) 60f else 50f
                                    configViewModel.updateWeight(pdfTestWeightGrams)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RealisticTexturesGalleryScreen(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MATERIAL FINISH TEXTURES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Procedural micro-textures, specular highlights, and holographic diffraction",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TactileButton(
                onClick = onNavigateBack,
                variant = TactileButtonVariant.OUTLINE,
                text = "Back"
            )
        }

        Text(
            text = "PRESET FINISHES",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        val finishes = RealisticFinishes.allFinishes

        finishes.forEach { finish ->
            TactileCard(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp, 45.dp)
                            .shadow(3.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRealisticFinishTexture(
                                baseColor = finish.baseColor,
                                accentColor = finish.accentColor,
                                pattern = finish.patternType
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = finish.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Finish: ${finish.finishType} • Pattern: ${finish.patternType.name.replace("_", " ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = finish.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun A4PdfVisualInspector(
    configViewModel: ConfiguratorViewModel,
    testWeightGrams: Float,
    onToggleWeight: () -> Unit
) {
    val currentConfig by configViewModel.currentConfig.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Controls Banner
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TRUE A4 (210 × 297 mm) DOCUMENT VIEWER",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Standard ISO 216 1:1.414 aspect ratio technical sheet",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }

                Button(
                    onClick = onToggleWeight,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0E3A68)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Test: ${testWeightGrams.toInt()}g (Click to toggle 50g/60g)", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // True A4 Paper Canvas (Aspect Ratio 1 : 1.4142)
        Surface(
            modifier = Modifier
                .widthIn(max = 595.dp)
                .fillMaxWidth()
                .aspectRatio(1f / 1.4142f)
                .shadow(12.dp, RoundedCornerShape(4.dp)),
            color = Color.White,
            shape = RoundedCornerShape(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // 1. Header: 7Hooks Vector Logo & Reference Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Canvas(modifier = Modifier.size(36.dp)) {
                            drawCircle(Color(0xFF0E3A68), radius = size.minDimension / 2.2f)
                            drawCircle(Color(0xFFEA580C), radius = size.minDimension / 4f, style = Stroke(width = 2.dp.toPx()))
                        }
                        Column {
                            Text(
                                text = "7HOOKS",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "PRECISION TACKLE ENGINEERING",
                                fontSize = 7.sp,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "DOC: ${currentConfig.referenceNumber}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "ISO A4 COMPLIANT SPEC",
                                fontSize = 7.sp,
                                color = Color(0xFF0E3A68),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFF0E3A68), thickness = 2.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Title Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TECHNICAL SPECIFICATION SHEET",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Product: ${currentConfig.productName} • Model: ${currentConfig.modelNumber}",
                            fontSize = 9.sp,
                            color = Color(0xFF475569)
                        )
                    }
                    Surface(
                        color = Color(0xFF0E3A68),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            text = "PRODUCTION APPROVED",
                            color = Color.White,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // CAD Drawing inside A4 sheet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                ) {
                    if (currentConfig.configType == ConfigType.JIG) {
                        JigEngineeringCanvas(config = currentConfig, modifier = Modifier.fillMaxSize())
                    } else {
                        RodEngineeringCanvas(config = currentConfig, modifier = Modifier.fillMaxSize())
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Technical Parameter Table
                Text(
                    text = "SPECIFICATION MATRIX",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0E3A68)
                )
                Spacer(modifier = Modifier.height(4.dp))

                val tableBg = Color(0xFFF8FAFC)
                val borderC = Color(0xFFE2E8F0)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderC)
                ) {
                    PdfTableRow("Product Identity", currentConfig.productName, "Model / Code", currentConfig.modelNumber, true)
                    PdfTableRow("Total Finished Mass", "${currentConfig.weightGrams.toInt()} g", "Overall Length", "${currentConfig.lengthMm.toInt()} mm", false)
                    PdfTableRow("Maximum Width / Dia", "${currentConfig.widthMm.toInt()} mm", "Primary Material", currentConfig.material, true)
                    PdfTableRow("Colorway & Finish", currentConfig.colorName, "Hardware Standard", "Stainless 316 / SiC", false)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notes & Quality Certification
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text("ENGINEERING NOTES & CERTIFICATION:", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        Text("• Manufactured to ±0.2mm CNC engineering tolerance.\n• Saltwater immersion anti-corrosion test compliant (ASTM B117 500h standard).\n• 7Hooks authentic proprietary hydrodynamic profile.", fontSize = 6.5.sp, color = Color(0xFF475569), lineHeight = 9.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer
                HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("7Hooks Precision Tackle Ltd. • ISO 9001 Certified", fontSize = 6.5.sp, color = Color(0xFF94A3B8))
                    Text("Page 1 of 1 • A4 210x297mm", fontSize = 6.5.sp, color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun PdfTableRow(label1: String, val1: String, label2: String, val2: String, isAlternate: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isAlternate) Color(0xFFF8FAFC) else Color.White)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Text("$label1: ", fontSize = 7.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Text(val1, fontSize = 7.5.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.weight(1f)) {
            Text("$label2: ", fontSize = 7.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Text(val2, fontSize = 7.5.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
        }
    }
}
