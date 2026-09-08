package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JigPatternType
import com.example.data.model.JigProduct
import com.example.data.model.ProductConfiguration
import com.example.ui.components.*
import com.example.viewmodel.ConfiguratorViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JigConfigScreen(
    configViewModel: ConfiguratorViewModel,
    onNavigateToEngineering: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentConfig by configViewModel.currentConfig.collectAsState()
    val selectedJig by configViewModel.selectedJigProduct.collectAsState()

    var isMoreOptionsExpanded by remember { mutableStateOf(false) }
    var isInlineTechnicalViewExpanded by remember { mutableStateOf(false) }
    var isProductInActionExpanded by remember { mutableStateOf(false) }

    var selectedConfigTab by remember { mutableIntStateOf(0) } // 0 = Custom Specs, 1 = Best Combination
    var activeTermInfo by remember { mutableStateOf<TechnicalTermInfo?>(null) }
    var selectedEyeColorName by remember { mutableStateOf("Luminous Lime") }

    val validation = remember(currentConfig.weightGrams, currentConfig.lengthMm, currentConfig.widthMm, selectedJig) {
        validateJigParameters(
            weight = currentConfig.weightGrams,
            length = currentConfig.lengthMm,
            width = currentConfig.widthMm,
            selectedJig = selectedJig
        )
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Configure Jig",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${currentConfig.lengthMm.toInt()} mm • ${currentConfig.weightGrams.toInt()} g",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${currentConfig.modelNumber} • FR: ${currentConfig.frontRing} • BR: ${currentConfig.backRing}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    TactileButton(
                        onClick = onNavigateToEngineering,
                        variant = TactileButtonVariant.PRIMARY,
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        text = "Technical Drawing",
                        testTag = "jig_continue_to_dashboard_button"
                    )
                }
            }
        },
        modifier = Modifier.testTag("jig_config_screen")
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isWideScreen = maxWidth >= 720.dp

            if (isWideScreen) {
                // TABLET / DESKTOP TWO-COLUMN RESPONSIVE LAYOUT
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // LEFT COLUMN: Product Header, Product Preview, Live Summary, Technical View Launch Card
                    Column(
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactProductHeader(config = currentConfig, selectedJig = selectedJig)

                        JigProductPreview(
                            config = currentConfig,
                            selectedJig = selectedJig
                        )

                        LiveConfigurationSummaryCard(config = currentConfig)

                        TechnicalViewLauncherCard(
                            config = currentConfig,
                            onOpenTechnicalView = onNavigateToEngineering,
                            onShowTermInfo = { activeTermInfo = it }
                        )
                    }

                    // RIGHT COLUMN: Step Indicator, Mode Tabs, Configuration Controls / Best Combination
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactStepPill(currentStep = 2, totalSteps = 3, label = "CONFIGURE SPECS")

                        // TAB ROW: CUSTOM SPECS VS BEST COMBINATION
                        TabRow(
                            selectedTabIndex = selectedConfigTab,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Tab(
                                selected = selectedConfigTab == 0,
                                onClick = { selectedConfigTab = 0 },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Text("Custom Specs", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            )
                            Tab(
                                selected = selectedConfigTab == 1,
                                onClick = { selectedConfigTab = 1 },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Text("Best Combination", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            )
                        }

                        if (selectedConfigTab == 0) {
                            // Validation warning / balanced indicator
                            JigValidationCard(
                                validation = validation,
                                onAutoCalibrate = {
                                    configViewModel.updateWeight(validation.goldenWeight)
                                    configViewModel.updateLength(validation.goldenLength)
                                    configViewModel.updateWidth(validation.goldenWidth)
                                    configViewModel.updateCustomWeight("")
                                    configViewModel.updateCustomLength("")
                                    configViewModel.updateCustomWidth("")
                                }
                            )

                            EssentialConfigurationControls(
                                config = currentConfig,
                                selectedJig = selectedJig,
                                configViewModel = configViewModel,
                                onShowTermInfo = { activeTermInfo = it }
                            )

                            MoreOptionsSection(
                                config = currentConfig,
                                selectedJig = selectedJig,
                                configViewModel = configViewModel,
                                isExpanded = isMoreOptionsExpanded,
                                onToggleExpand = { isMoreOptionsExpanded = !isMoreOptionsExpanded },
                                selectedEyeColorName = selectedEyeColorName,
                                onSelectEyeColor = { name, _ -> selectedEyeColorName = name },
                                onShowTermInfo = { activeTermInfo = it }
                            )
                        } else {
                            // Best combinations curated presets
                            BestCombinationsSection(
                                config = currentConfig,
                                selectedJig = selectedJig,
                                onApplyPreset = { preset ->
                                    configViewModel.updateWeight(preset.weightGrams)
                                    configViewModel.updateLength(preset.lengthMm)
                                    configViewModel.updateWidth(preset.widthMm)
                                    configViewModel.updateCustomWeight("")
                                    configViewModel.updateCustomLength("")
                                    configViewModel.updateCustomWidth("")
                                    configViewModel.updateFinishType(preset.finishType)
                                    configViewModel.updateFrontRing(preset.frontRing)
                                    configViewModel.updateBackRing(preset.backRing)
                                    configViewModel.updateHookTypeJig(preset.hookType)
                                    configViewModel.updateEyeStyle(preset.eyeStyle)
                                    configViewModel.updateAssistCord(preset.assistCord)
                                }
                            )
                        }

                        ProductInActionSection(
                            config = currentConfig,
                            isExpanded = isProductInActionExpanded,
                            onToggleExpand = { isProductInActionExpanded = !isProductInActionExpanded }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                // MOBILE COMPACT VERTICAL LAYOUT (Strict Vertical Scroll with Bounded Preview)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // 1. Compact Step Indicator
                    CompactStepPill(currentStep = 2, totalSteps = 3, label = "CONFIGURE SPECS")

                    // 2. Product Name & Model Header
                    CompactProductHeader(config = currentConfig, selectedJig = selectedJig)

                    // 3. PRODUCT PREVIEW (Exact selected jig photo with bounded height)
                    JigProductPreview(
                        config = currentConfig,
                        selectedJig = selectedJig
                    )

                    // 4. LIVE CONFIGURATION SUMMARY
                    LiveConfigurationSummaryCard(config = currentConfig)

                    // 5. CONFIGURATION MODE TABS (Custom Specs vs Best Combination)
                    TabRow(
                        selectedTabIndex = selectedConfigTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = selectedConfigTab == 0,
                            onClick = { selectedConfigTab = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Custom Specs", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        )
                        Tab(
                            selected = selectedConfigTab == 1,
                            onClick = { selectedConfigTab = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Best Combination", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        )
                    }

                    if (selectedConfigTab == 0) {
                        // 6. HYDRODYNAMIC & BREAKAGE VALIDATION CARD
                        JigValidationCard(
                            validation = validation,
                            onAutoCalibrate = {
                                configViewModel.updateWeight(validation.goldenWeight)
                                configViewModel.updateLength(validation.goldenLength)
                                configViewModel.updateWidth(validation.goldenWidth)
                                configViewModel.updateCustomWeight("")
                                configViewModel.updateCustomLength("")
                                configViewModel.updateCustomWidth("")
                            }
                        )

                        // 7. ESSENTIAL CONFIGURATION CONTROLS
                        EssentialConfigurationControls(
                            config = currentConfig,
                            selectedJig = selectedJig,
                            configViewModel = configViewModel,
                            onShowTermInfo = { activeTermInfo = it }
                        )

                        // 8. MORE OPTIONS (Progressive Disclosure Accordion)
                        MoreOptionsSection(
                            config = currentConfig,
                            selectedJig = selectedJig,
                            configViewModel = configViewModel,
                            isExpanded = isMoreOptionsExpanded,
                            onToggleExpand = { isMoreOptionsExpanded = !isMoreOptionsExpanded },
                            selectedEyeColorName = selectedEyeColorName,
                            onSelectEyeColor = { name, _ -> selectedEyeColorName = name },
                            onShowTermInfo = { activeTermInfo = it }
                        )
                    } else {
                        // BEST COMBINATION PRESETS
                        BestCombinationsSection(
                            config = currentConfig,
                            selectedJig = selectedJig,
                            onApplyPreset = { preset ->
                                configViewModel.updateWeight(preset.weightGrams)
                                configViewModel.updateLength(preset.lengthMm)
                                configViewModel.updateWidth(preset.widthMm)
                                configViewModel.updateCustomWeight("")
                                configViewModel.updateCustomLength("")
                                configViewModel.updateCustomWidth("")
                                configViewModel.updateFinishType(preset.finishType)
                                configViewModel.updateFrontRing(preset.frontRing)
                                configViewModel.updateBackRing(preset.backRing)
                                configViewModel.updateHookTypeJig(preset.hookType)
                                configViewModel.updateEyeStyle(preset.eyeStyle)
                                configViewModel.updateAssistCord(preset.assistCord)
                            }
                        )
                    }

                    // 9. TECHNICAL VIEW LAUNCHER CARD
                    TechnicalViewLauncherCard(
                        config = currentConfig,
                        onOpenTechnicalView = onNavigateToEngineering,
                        isInlineExpanded = isInlineTechnicalViewExpanded,
                        onToggleInline = { isInlineTechnicalViewExpanded = !isInlineTechnicalViewExpanded },
                        onShowTermInfo = { activeTermInfo = it }
                    )

                    // 10. PRODUCT IN ACTION (Collapsed presentation)
                    ProductInActionSection(
                        config = currentConfig,
                        isExpanded = isProductInActionExpanded,
                        onToggleExpand = { isProductInActionExpanded = !isProductInActionExpanded }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Technical Term Information Modal Dialog
        TechnicalTermDialog(
            termInfo = activeTermInfo,
            onDismiss = { activeTermInfo = null }
        )
    }
}

/**
 * Compact Step Indicator Pill (Restrained, doesn't eat vertical space)
 */
@Composable
private fun CompactStepPill(currentStep: Int, totalSteps: Int, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "STEP $currentStep OF $totalSteps",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "7Hooks Factory Specs",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Compact Product Header:
 * Product Name (clear, moderately strong), Model (smaller secondary text), clean tag
 * No oversized OEM SPEC badge
 */
@Composable
private fun CompactProductHeader(config: ProductConfiguration, selectedJig: JigProduct) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 1.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Model: ${config.modelNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = selectedJig.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = "COMMERCIAL JIG",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * Live Configuration Summary:
 * Compact summary near the configuration controls with live custom spec indicators
 */
@Composable
private fun LiveConfigurationSummaryCard(config: ProductConfiguration) {
    val frDisplay = if (config.frontRing == "Custom" && config.customFrontRing.isNotEmpty()) "FR: ${config.customFrontRing}" else "FR: ${config.frontRing}"
    val brDisplay = if (config.backRing == "Custom" && config.customBackRing.isNotEmpty()) "BR: ${config.customBackRing}" else "BR: ${config.backRing}"
    val cordDisplay = if (config.threadColor == "Custom" && config.customAssistCordColor.isNotEmpty()) "Cord: ${config.customAssistCordColor}" else "Cord: ${config.threadColor}"
    val finishDisplay = if (config.finishType == "Custom" && config.customFinish.isNotEmpty()) config.customFinish else config.finishType

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONFIGURED SPECIFICATION",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${config.weightGrams.toInt()} g • ${config.lengthMm.toInt()} mm • ${config.widthMm.toInt()} mm",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$finishDisplay • ${config.colorName}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$frDisplay  |  $brDisplay  |  $cordDisplay",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Essential Configuration Section:
 * Weight, Length, Width, Finish & Colors, Front & Back Rings, Hook, Thread/Cord
 * Compact professional controls (segmented selectors, compact steppers, swatches, custom fields)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EssentialConfigurationControls(
    config: ProductConfiguration,
    selectedJig: JigProduct,
    configViewModel: ConfiguratorViewModel,
    onShowTermInfo: (TechnicalTermInfo) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 1. WEIGHT CONFIGURATION (Supported options + Custom Stepper/Input)
        var isCustomWeightActive by remember(config.customWeight) {
            mutableStateOf(config.customWeight.isNotEmpty())
        }

        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.5.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "WEIGHT (FINISHED MASS)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        TechnicalInfoIcon(
                            term = "Finished Mass (Weight in Grams)",
                            definition = "The solid mass of the jig alloy core after surface plating and clear coat. In saltwater vertical jigging, heavier jigs sink faster to punch through thermoclines and drift currents, while lighter jigs stay in the strike zone longer.",
                            recommendation = "Select 1 to 1.5 grams per meter of water depth as a factory standard rule of thumb.",
                            onShowInfo = onShowTermInfo
                        )
                    }
                    Text(
                        text = if (isCustomWeightActive && config.customWeight.isNotEmpty()) "${config.customWeight} g (Custom)" else "${config.weightGrams.toInt()} g",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Supported Weight Chips + Custom Chip
                val supportedWeights = listOf(40f, 50f, 60f, 80f, 100f, 120f, 150f, 200f)
                    .filter { it in selectedJig.minWeightGrams..selectedJig.maxWeightGrams }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    supportedWeights.forEach { weightVal ->
                        val isSelected = !isCustomWeightActive && config.weightGrams == weightVal
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                isCustomWeightActive = false
                                configViewModel.updateCustomWeight("")
                                configViewModel.updateWeight(weightVal)
                            },
                            label = { Text("${weightVal.toInt()}g", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }

                    // Custom Weight Chip
                    FilterChip(
                        selected = isCustomWeightActive,
                        onClick = { isCustomWeightActive = true },
                        label = { Text("Custom", fontSize = 11.sp, fontWeight = if (isCustomWeightActive) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }

                if (isCustomWeightActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = config.customWeight,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == '.' }
                            configViewModel.updateCustomWeight(filtered)
                            filtered.toFloatOrNull()?.let { configViewModel.updateWeight(it) }
                        },
                        label = { Text("Custom Weight in Grams (e.g. 118)", fontSize = 12.sp) },
                        placeholder = { Text("Enter exact grams") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = config.weightGrams,
                        onValueChange = { configViewModel.updateWeight(it) },
                        valueRange = selectedJig.minWeightGrams..selectedJig.maxWeightGrams,
                        steps = ((selectedJig.maxWeightGrams - selectedJig.minWeightGrams) / 5f).toInt() - 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .testTag("jig_weight_slider")
                    )
                }
            }
        }

        // 2. LENGTH & WIDTH (Compact dual-dimension card + Custom Millimeters entry)
        var showCustomDimensions by remember {
            mutableStateOf(config.customLength.isNotEmpty() || config.customWidth.isNotEmpty())
        }

        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.5.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Length Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "TOTAL LENGTH",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        TechnicalInfoIcon(
                            term = "Total Body Length (mm)",
                            definition = "The axial length from the tip of the front tow eye to the base of the rear split ring eyelet.",
                            recommendation = "Longer bodies produce erratic wide darting (knife action), while shorter bodies flutter rapidly on the drop.",
                            onShowInfo = onShowTermInfo
                        )
                    }
                    Text(
                        text = if (config.customLength.isNotEmpty()) "${config.customLength} mm (Custom)" else "${config.lengthMm.toInt()} mm",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Slider(
                    value = config.lengthMm,
                    onValueChange = {
                        configViewModel.updateLength(it)
                        configViewModel.updateCustomLength("")
                    },
                    valueRange = selectedJig.minLengthMm..selectedJig.maxLengthMm,
                    steps = ((selectedJig.maxLengthMm - selectedJig.minLengthMm) / 5f).toInt() - 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .testTag("jig_length_slider")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Width Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "HYDRODYNAMIC WIDTH",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        TechnicalInfoIcon(
                            term = "Hydrodynamic Keel Width (mm)",
                            definition = "The maximum transverse width across the jig belly keel.",
                            recommendation = "A wider belly slows the descent and generates erratic side-to-side flutter on slack line.",
                            onShowInfo = onShowTermInfo
                        )
                    }
                    Text(
                        text = if (config.customWidth.isNotEmpty()) "${config.customWidth} mm (Custom)" else "${config.widthMm.toInt()} mm",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Slider(
                    value = config.widthMm,
                    onValueChange = {
                        configViewModel.updateWidth(it)
                        configViewModel.updateCustomWidth("")
                    },
                    valueRange = selectedJig.minWidthMm..selectedJig.maxWidthMm,
                    steps = ((selectedJig.maxWidthMm - selectedJig.minWidthMm) / 1f).toInt() - 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .testTag("jig_width_slider")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Dimensions Toggle & Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { showCustomDimensions = !showCustomDimensions },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (showCustomDimensions) "Hide Custom Dimensions" else "Enter Custom mm Overrides",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (showCustomDimensions) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = config.customLength,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() || it == '.' }
                                configViewModel.updateCustomLength(filtered)
                                filtered.toFloatOrNull()?.let { configViewModel.updateLength(it) }
                            },
                            label = { Text("Custom Length (mm)", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = config.customWidth,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() || it == '.' }
                                configViewModel.updateCustomWidth(filtered)
                                filtered.toFloatOrNull()?.let { configViewModel.updateWidth(it) }
                            },
                            label = { Text("Custom Width (mm)", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. FINISH & COLORS (Compact Swatches & Finish Chips + Custom Finish)
        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.5.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "FINISH & COLOR PALETTE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    TechnicalInfoIcon(
                        term = "Surface Finish & Holographic Coating",
                        definition = "Multi-layered vacuum metalized plating with UV reactive topcoat.",
                        recommendation = "Matches prey forage light transmission at varying sea depths.",
                        onShowInfo = onShowTermInfo
                    )
                }
                Text(
                    text = "Protective coating and light reflection treatment.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Finishes (Solid, Metallic, Matte, Glow, Holographic, UV Reactive, Custom)
                val finishes = listOf(
                    "High-Gloss Metallic",
                    "Solid Color",
                    "Matte Stealth",
                    "Luminous Glow",
                    "Holographic Flash",
                    "UV Reactive",
                    "Custom"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    finishes.forEach { fin ->
                        val isSelected = config.finishType == fin
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                configViewModel.updateFinish(
                                    finishName = fin,
                                    baseHex = config.baseColorHex,
                                    accentHex = config.accentColorHex,
                                    patternType = when {
                                        fin.contains("Holographic", ignoreCase = true) -> JigPatternType.HOLOGRAPHIC_SLASH
                                        fin.contains("Glow", ignoreCase = true) -> JigPatternType.DOT_PATTERN
                                        else -> JigPatternType.SOLID_STRIPE
                                    }
                                )
                            },
                            label = { Text(fin, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                if (config.finishType == "Custom") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = config.customFinish,
                        onValueChange = { configViewModel.updateCustomFinish(it) },
                        label = { Text("Custom Finish (e.g. Chameleon Flip-Flop Pearl)", fontSize = 11.5.sp) },
                        placeholder = { Text("Enter custom finish specification") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Realistic Presets Selector
                RealisticFinishSelector(
                    selectedColorName = config.colorName,
                    onSelectFinish = { finish ->
                        configViewModel.updateFinish(
                            finishName = finish.name.substringBefore(" /"),
                            baseHex = (finish.baseColor.value shr 32).toLong(),
                            accentHex = (finish.accentColor.value shr 32).toLong(),
                            patternType = finish.patternType
                        )
                    }
                )
            }
        }

        // 4. ATTACHMENT RINGS (FRONT RING & BACK RING) - FlowRow prevents any clipping
        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.5.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ATTACHMENT RINGS (FRONT & BACK)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        TechnicalInfoIcon(
                            term = "Solid & Split Rigging Rings",
                            definition = "SUS304 forged stainless steel seamless solid front ring and heavy-duty rear split ring.",
                            recommendation = "Heavy-duty rings prevent deformation under extreme drag loads and violent predatory headshakes.",
                            onShowInfo = onShowTermInfo
                        )
                    }
                    Text(
                        text = "SUS304 STAINLESS",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Front Ring Selector
                Text(
                    text = "Front Ring (Line Tie)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Front attachment point of the Jig for leader line tie.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("None", "Standard", "Heavy Duty", "Custom").forEach { ringOption ->
                        val isSelected = config.frontRing == ringOption
                        FilterChip(
                            selected = isSelected,
                            onClick = { configViewModel.updateFrontRing(ringOption) },
                            label = { Text(ringOption, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            } else null,
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                if (config.frontRing == "Custom") {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = config.customFrontRing,
                        onValueChange = { configViewModel.updateCustomFrontRing(it) },
                        label = { Text("Custom Front Ring (e.g. #7 Heavy Forged Titanium)", fontSize = 11.5.sp) },
                        placeholder = { Text("Enter custom front ring specification") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Back Ring Selector
                Text(
                    text = "Back Ring (Rear Stinger)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Rear attachment ring for tail stinger hook or teaser blade.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("None", "Standard", "Heavy Duty", "Custom").forEach { ringOption ->
                        val isSelected = config.backRing == ringOption
                        FilterChip(
                            selected = isSelected,
                            onClick = { configViewModel.updateBackRing(ringOption) },
                            label = { Text(ringOption, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            } else null,
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                if (config.backRing == "Custom") {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = config.customBackRing,
                        onValueChange = { configViewModel.updateCustomBackRing(it) },
                        label = { Text("Custom Back Ring (e.g. #5 Solid Stinger Ring)", fontSize = 11.5.sp) },
                        placeholder = { Text("Enter custom back ring specification") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 5. HOOK RIGGING
        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.5.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "HOOK RIGGING SPECIFICATION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    TechnicalInfoIcon(
                        term = "Assist Hook Rigging",
                        definition = "Saltwater forged chemically sharpened assist hooks attached via braided cord.",
                        recommendation = "Top assist hooks target predatory fish attacking the head during the jig pause.",
                        onShowInfo = onShowTermInfo
                    )
                }
                Text(
                    text = "Hook attached to the Jig for strikes.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                val availableHooks = (selectedJig.availableHooks + listOf("Custom")).distinct()

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableHooks.forEach { hook ->
                        val isSelected = config.hookTypeJig == hook
                        FilterChip(
                            selected = isSelected,
                            onClick = { configViewModel.updateHook(hook) },
                            label = { Text(hook, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            } else null,
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                if (config.hookTypeJig == "Custom") {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = config.customHook,
                        onValueChange = { configViewModel.updateCustomHook(it) },
                        label = { Text("Custom Hook (e.g. Gamakatsu Heavy Jig 4/0)", fontSize = 11.5.sp) },
                        placeholder = { Text("Enter custom hook specification") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 6. ASSIST CORD / THREAD BINDING COLOR (Requirement: clear terminology & explanation)
        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.5.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ASSIST CORD / THREAD BINDING COLOR",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    TechnicalInfoIcon(
                        term = "Assist Cord Filament & Thread Binding",
                        definition = "High-tensile Ultra-High Molecular Weight Polyethylene (PE) or Kevlar line securing the hook eye to the solid ring.",
                        recommendation = "Bright color binding acts as a hot-spot strike trigger in low light conditions.",
                        onShowInfo = onShowTermInfo
                    )
                }
                Text(
                    text = "Color of the cord attached to the Assist Hook.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                ThreadSelector(
                    selectedThread = config.threadColor,
                    onSelectThread = { threadOption ->
                        configViewModel.updateThread(
                            threadColor = threadOption.name,
                            colorHex = threadOption.color?.let { (it.value shr 32).toLong() }
                        )
                    }
                )

                if (config.threadColor == "Custom") {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = config.customAssistCordColor,
                        onValueChange = { configViewModel.updateCustomAssistCordColor(it) },
                        label = { Text("Custom Assist Cord Specification (e.g. UV Fluorescent Chartreuse PE)", fontSize = 11.5.sp) },
                        placeholder = { Text("Enter custom assist cord details") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * More Options Progressive Disclosure Accordion:
 * Secondary manufacturing parameters (Core Alloy, 3D Strike Eye, Assist Cord, ISO Tolerance)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoreOptionsSection(
    config: ProductConfiguration,
    selectedJig: JigProduct,
    configViewModel: ConfiguratorViewModel,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    selectedEyeColorName: String,
    onSelectEyeColor: (String, Color) -> Unit,
    onShowTermInfo: (TechnicalTermInfo) -> Unit
) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 1.5.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "MORE OPTIONS (ALLOY, EYE & CORD)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. ALLOY & CORE CONSTRUCTION
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Core Alloy Construction:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TechnicalInfoIcon(
                            term = "Core Alloy Composition",
                            definition = "High-purity antimony-hardened lead alloy or lead-free tungsten matrix providing density and flexural rigidity.",
                            recommendation = "Hardened alloy prevents bending under violent fish headshakes and reef strikes.",
                            onShowInfo = onShowTermInfo
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedJig.materials.forEach { mat ->
                            val isSelected = config.material == mat
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateMaterial(mat) },
                                label = { Text(mat, fontSize = 11.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                } else null,
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    // 2. 3D EYE STYLE
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "3D Strike Eye Specification:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TechnicalInfoIcon(
                            term = "3D Luminous Strike Eye",
                            definition = "Optically clear domed resin lens with UV luminous or holographic iris reflection.",
                            recommendation = "Predators target the eye as the vulnerability trigger point when attacking from below.",
                            onShowInfo = onShowTermInfo
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "3D Luminous Strike Eye",
                            "Holographic Foil Eye",
                            "High-Contrast Target Eye",
                            "Custom Etched Eye"
                        ).forEach { eye ->
                            val isSelected = config.eyeStyle == eye
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateEyeStyle(eye) },
                                label = { Text(eye, fontSize = 11.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                } else null,
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    // Live 3D Eye Preview Box
                    StrikeEyePreviewBox(
                        eyeStyle = config.eyeStyle,
                        selectedEyeColorName = selectedEyeColorName,
                        onSelectEyeColor = onSelectEyeColor
                    )

                    // 3. ASSIST CORD MATERIAL (OPTIONAL)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Assist Cord Tensile Rigging (Optional):",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TechnicalInfoIcon(
                            term = "Assist Cord Tensile Rating",
                            definition = "Breaking strain rating and core material of the assist cord rigging.",
                            recommendation = "Optional parameter. Choose 'None' for unrigged jigs or 150lb to 250lb depending on target pelagic species.",
                            onShowInfo = onShowTermInfo
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "None (Optional / Unrigged)",
                            "Braided PE (200 lb)",
                            "Kevlar Core (250 lb)",
                            "Wire Assist (150 lb)",
                            "Fluorocarbon Core (180 lb)"
                        ).forEach { cord ->
                            val isSelected = config.assistCord == cord
                            FilterChip(
                                selected = isSelected,
                                onClick = { configViewModel.updateAssistCord(cord) },
                                label = { Text(cord, fontSize = 11.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                } else null,
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    // 4. MANUFACTURING TOLERANCE NOTE
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "MANUFACTURING TOLERANCE",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                TechnicalInfoIcon(
                                    term = "ISO 2768-m Machining Tolerance",
                                    definition = "General dimensional and geometric tolerances for metal die casting and CNC mold tooling.",
                                    recommendation = "Ensures weight consistency within ±1.5% and axial symmetry across batch production runs.",
                                    onShowInfo = onShowTermInfo
                                )
                            }
                            Text(
                                text = "ISO 2768-m (±0.2mm)",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Technical View Launcher Card:
 * Opens the separate technical drawing / specification view using the exact SAME configuration.
 * Redesigned to eliminate truncation and provide clear call-to-action button.
 */
@Composable
private fun TechnicalViewLauncherCard(
    config: ProductConfiguration,
    onOpenTechnicalView: () -> Unit,
    isInlineExpanded: Boolean = false,
    onToggleInline: (() -> Unit)? = null,
    onShowTermInfo: ((TechnicalTermInfo) -> Unit)? = null
) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 1.5.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Architecture,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "TECHNICAL SPECIFICATION VIEW",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            if (onShowTermInfo != null) {
                                TechnicalInfoIcon(
                                    term = "1:1 Orthographic CAD Projection",
                                    definition = "Precision engineering drawing displaying frontal, lateral, and cross-sectional views with dimensional tolerances.",
                                    recommendation = "Used by CNC toolmakers to cut high-pressure steel injection dies.",
                                    onShowInfo = onShowTermInfo
                                )
                            }
                        }
                        Text(
                            text = "1:1 Orthographic CAD Projection & Tolerances",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // High-contrast, full-width action button - NEVER truncated to "Op"
            TactileButton(
                onClick = onOpenTechnicalView,
                variant = TactileButtonVariant.PRIMARY,
                icon = Icons.Default.Architecture,
                text = "View Engineering Blueprint",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            )

            if (onToggleInline != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onToggleInline() }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isInlineExpanded) "Hide Inline CAD Blueprint" else "Show Inline CAD Blueprint Preview",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (isInlineExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(
                    visible = isInlineExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        JigEngineeringCanvas(config = config)
                    }
                }
            }
        }
    }
}

/**
 * Product in Action Section (Clean Collapsible Presentation)
 */
@Composable
private fun ProductInActionSection(
    config: ProductConfiguration,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    TactileCard(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 1.5.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF0F2942),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "VISUAL PRESENTATION",
                            color = Color(0xFF38BDF8),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Product in Action",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    JigFishingAnimation(config = config)
                }
            }
        }
    }
}
