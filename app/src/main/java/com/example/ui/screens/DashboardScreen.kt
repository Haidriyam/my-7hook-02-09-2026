package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.R
import com.example.data.local.SavedConfigEntity
import com.example.data.model.UserAccount
import com.example.ui.components.*
import com.example.viewmodel.ConfiguratorViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUser: UserAccount?,
    configViewModel: ConfiguratorViewModel,
    onNavigateToJigs: () -> Unit,
    onNavigateToRods: () -> Unit,
    onNavigateToLures: () -> Unit = {},
    onNavigateToPackaging: () -> Unit,
    onNavigateToEngineering: (SavedConfigEntity) -> Unit,
    onLogout: () -> Unit,
    onNavigateToVisualQa: () -> Unit = {}
) {
    val savedConfigs by configViewModel.savedConfigs.collectAsState()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(290.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Drawer Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Official7HooksLogo(
                        height = 34.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentUser?.name ?: "Technical Configurator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentUser?.company?.ifEmpty { "7Hooks OEM Manufacturing" } ?: "7Hooks OEM Manufacturing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home", fontWeight = FontWeight.SemiBold) },
                    selected = true,
                    onClick = { coroutineScope.launch { drawerState.close() } },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawer_nav_home")
                )

                NavigationDrawerItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_jig_icon),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Jigs", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            onNavigateToJigs()
                        }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawer_nav_jigs")
                )

                NavigationDrawerItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_rod_icon),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Rods", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            onNavigateToRods()
                        }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawer_nav_rods")
                )

                NavigationDrawerItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_lure_icon),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Lures", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            onNavigateToLures()
                        }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawer_nav_lures")
                )

                NavigationDrawerItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_package_box),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Packaging", fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            onNavigateToPackaging()
                        }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawer_nav_packaging")
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    label = { Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            onLogout()
                        }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawer_nav_logout")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                AppHeader(
                    title = "Product Studio",
                    onMenuClick = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    currentUser = currentUser,
                    onLogoutClick = onLogout
                )
            },
            modifier = Modifier.testTag("dashboard_screen")
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // High-End Industrial Brand Header Block (Clean, professional, non-generic)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Product Studio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Commercial specifications & ISO technical schematics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // 4-MODULE RESPONSIVE SECTION (Jigs, Rods, Lures, Packaging)
                item {
                    ModuleCardsSection(
                        onNavigateToJigs = onNavigateToJigs,
                        onNavigateToRods = onNavigateToRods,
                        onNavigateToLures = onNavigateToLures,
                        onNavigateToPackaging = onNavigateToPackaging
                    )
                }

                // SAVED CONFIGURATIONS SECTION
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SAVED CONFIGURATIONS & BLUEPRINTS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "${savedConfigs.size} archived",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (savedConfigs.isEmpty()) {
                    item {
                        TactileCard(
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No saved configurations yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Configure a Jig, Rod, or Lure and tap 'Save' on the CAD screen to archive blueprints.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                } else {
                    items(savedConfigs) { configEntity ->
                        SavedConfigItemCard(
                            configEntity = configEntity,
                            onOpen = {
                                configViewModel.loadSavedConfig(configEntity)
                                onNavigateToEngineering(configEntity)
                            },
                            onDelete = {
                                configViewModel.deleteSavedConfig(configEntity.configId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedConfigItemCard(
    configEntity: SavedConfigEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(configEntity.timestamp) {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(configEntity.timestamp))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .clickable { onOpen() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 0.5.dp
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (configEntity.configType) {
                                "JIG" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                "ROD" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (configEntity.configType) {
                        "JIG" -> {
                            Image(
                                painter = painterResource(id = R.drawable.ic_jig_icon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        "ROD" -> {
                            Image(
                                painter = painterResource(id = R.drawable.ic_rod_icon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.ic_lure_icon),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = configEntity.productName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Ref: ${configEntity.referenceNumber} • $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (configEntity.configType == "LURE") {
                            "Specs: ${configEntity.lengthMm.toInt()}mm | ${String.format(Locale.US, "%.1f", configEntity.weightGrams)}g | Lip: ${String.format(Locale.US, "%.1f", configEntity.divingDepthMeters)}m"
                        } else {
                            "Specs: ${configEntity.lengthMm.toInt()}mm | ${configEntity.weightGrams.toInt()}g | ${configEntity.material}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete configuration",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ModuleCardsSection(
    onNavigateToJigs: () -> Unit,
    onNavigateToRods: () -> Unit,
    onNavigateToLures: () -> Unit,
    onNavigateToPackaging: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProductModuleStudioTile(
                    title = "Jigs",
                    subtitle = "Deep-drop & casting ballast",
                    imageModel = com.example.data.model.ProductCatalog.CATEGORY_IMAGE_JIG,
                    fallbackRes = R.drawable.ic_jig_icon,
                    testTag = "dashboard_jig_configurator_card",
                    buttonTag = "configure_jig_button",
                    onClick = onNavigateToJigs,
                    modifier = Modifier.weight(1f)
                )
                ProductModuleStudioTile(
                    title = "Rods",
                    subtitle = "Carbon composite blanks",
                    imageModel = com.example.data.model.ProductCatalog.CATEGORY_IMAGE_ROD,
                    fallbackRes = R.drawable.ic_rod_icon,
                    testTag = "dashboard_rod_configurator_card",
                    buttonTag = "configure_rod_button",
                    onClick = onNavigateToRods,
                    modifier = Modifier.weight(1f)
                )
                ProductModuleStudioTile(
                    title = "Lures",
                    subtitle = "Hardbody & topwater action",
                    imageModel = com.example.data.model.ProductCatalog.CATEGORY_IMAGE_LURE,
                    fallbackRes = R.drawable.ic_lure_icon,
                    testTag = "dashboard_lure_configurator_card",
                    buttonTag = "configure_lure_button",
                    onClick = onNavigateToLures,
                    modifier = Modifier.weight(1f)
                )
                ProductModuleStudioTile(
                    title = "Packaging",
                    subtitle = "Custom retail blister & boxes",
                    imageModel = com.example.data.model.ProductCatalog.CATEGORY_IMAGE_PACKAGING,
                    fallbackRes = R.drawable.ic_package_box,
                    testTag = "dashboard_packaging_card",
                    buttonTag = "configure_packaging_button",
                    onClick = onNavigateToPackaging,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProductModuleStudioTile(
                        title = "Jigs",
                        subtitle = "Deep-drop & casting ballast",
                        imageModel = com.example.data.model.ProductCatalog.CATEGORY_IMAGE_JIG,
                        fallbackRes = R.drawable.ic_jig_icon,
                        testTag = "dashboard_jig_configurator_card",
                        buttonTag = "configure_jig_button",
                        onClick = onNavigateToJigs,
                        modifier = Modifier.weight(1f)
                    )
                    ProductModuleStudioTile(
                        title = "Rods",
                        subtitle = "Carbon composite blanks",
                        imageModel = com.example.data.model.ProductCatalog.CATEGORY_IMAGE_ROD,
                        fallbackRes = R.drawable.ic_rod_icon,
                        testTag = "dashboard_rod_configurator_card",
                        buttonTag = "configure_rod_button",
                        onClick = onNavigateToRods,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProductModuleStudioTile(
                        title = "Lures",
                        subtitle = "Hardbody & topwater action",
                        imageModel = com.example.data.model.ProductCatalog.CATEGORY_IMAGE_LURE,
                        fallbackRes = R.drawable.ic_lure_icon,
                        testTag = "dashboard_lure_configurator_card",
                        buttonTag = "configure_lure_button",
                        onClick = onNavigateToLures,
                        modifier = Modifier.weight(1f)
                    )
                    ProductModuleStudioTile(
                        title = "Packaging",
                        subtitle = "Custom retail blister & boxes",
                        imageModel = com.example.data.model.ProductCatalog.CATEGORY_IMAGE_PACKAGING,
                        fallbackRes = R.drawable.ic_package_box,
                        testTag = "dashboard_packaging_card",
                        buttonTag = "configure_packaging_button",
                        onClick = onNavigateToPackaging,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Compact, precision-focused studio tile for high-end industrial CAD software.
 */
@Composable
private fun ProductModuleStudioTile(
    title: String,
    subtitle: String,
    imageModel: Any?,
    fallbackRes: Int,
    testTag: String,
    buttonTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(10.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clean, restrained product illustration frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = title,
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    error = painterResource(id = fallbackRes),
                    fallback = painterResource(id = fallbackRes),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.5.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Subtle, compact action line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(buttonTag),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configure",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
