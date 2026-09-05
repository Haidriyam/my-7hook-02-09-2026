package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

                    Spacer(modifier = Modifier.height(12.dp))
                    ThemeSelectorRow()
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
                    label = { Text("Hard Lures", fontWeight = FontWeight.SemiBold) },
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

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                    label = { Text("Visual QA Studio", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            onNavigateToVisualQa()
                        }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawer_nav_visual_qa")
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
                    title = "Dashboard",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
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
                        ThemeSelectorRow()
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

    TactileCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shadowElevation = 2.dp
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
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (configEntity.configType) {
                                "JIG" -> MaterialTheme.colorScheme.primaryContainer
                                "ROD" -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.tertiaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when (configEntity.configType) {
                        "JIG" -> {
                            Image(
                                painter = painterResource(id = R.drawable.ic_jig_icon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        "ROD" -> {
                            Image(
                                painter = painterResource(id = R.drawable.ic_rod_icon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.ic_lure_icon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = configEntity.productName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete configuration",
                    tint = MaterialTheme.colorScheme.error
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
        val isMobile = maxWidth < 480.dp
        if (isMobile) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactModuleRow(
                    title = "Jigheads & Pelagic Spoons",
                    subtitle = "Tungsten, lead heads, 3D eyes & powder finishes",
                    iconRes = R.drawable.ic_jig_icon,
                    gradient = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
                    buttonVariant = TactileButtonVariant.PRIMARY,
                    onClick = onNavigateToJigs,
                    testTag = "dashboard_jig_configurator_card"
                )
                CompactModuleRow(
                    title = "Toray Custom Rod Blanks",
                    subtitle = "Carbon specs, guides, reel seats & deflection",
                    iconRes = R.drawable.ic_rod_icon,
                    gradient = listOf(Color(0xFFEA580C), Color(0xFFC2410C)),
                    buttonVariant = TactileButtonVariant.SECONDARY,
                    onClick = onNavigateToRods,
                    testTag = "dashboard_rod_configurator_card"
                )
                CompactModuleRow(
                    title = "Hard Lures & Crankbaits",
                    subtitle = "ABS shells, weight transfer & diving lip depth",
                    iconRes = R.drawable.ic_lure_icon,
                    gradient = listOf(Color(0xFF0D9488), Color(0xFF0F766E)),
                    buttonVariant = TactileButtonVariant.PRIMARY,
                    onClick = onNavigateToLures,
                    testTag = "dashboard_lure_configurator_card"
                )
                CompactModuleRow(
                    title = "Retail Packaging Systems",
                    subtitle = "Die-cut blister cards, polybags & custom branding",
                    iconRes = R.drawable.ic_package_box,
                    gradient = listOf(Color(0xFF475569), Color(0xFF1E293B)),
                    buttonVariant = TactileButtonVariant.OUTLINE,
                    onClick = onNavigateToPackaging,
                    testTag = "dashboard_packaging_card"
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    JigConfigDashboardCard(
                        modifier = Modifier.weight(1f),
                        onNavigateToJigs = onNavigateToJigs
                    )
                    RodConfigDashboardCard(
                        modifier = Modifier.weight(1f),
                        onNavigateToRods = onNavigateToRods
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LureConfigDashboardCard(
                        modifier = Modifier.weight(1f),
                        onNavigateToLures = onNavigateToLures
                    )
                    PackagingConfigDashboardCard(
                        modifier = Modifier.weight(1f),
                        onNavigateToPackaging = onNavigateToPackaging
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactModuleRow(
    title: String,
    subtitle: String,
    @androidx.annotation.DrawableRes iconRes: Int,
    gradient: List<Color>,
    buttonVariant: TactileButtonVariant,
    onClick: () -> Unit,
    testTag: String
) {
    TactileCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable { onClick() },
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TactileButton(
                onClick = onClick,
                variant = buttonVariant,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                text = "Configure"
            )
        }
    }
}

@Composable
private fun JigConfigDashboardCard(
    modifier: Modifier = Modifier,
    onNavigateToJigs: () -> Unit
) {
    TactileCard(
        modifier = modifier
            .testTag("dashboard_jig_configurator_card")
            .clickable { onNavigateToJigs() },
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_jig_icon),
                        contentDescription = "Fishing Jig Icon",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = "Jigs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Text(
                    text = "Jigheads, spoons & pelagics",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }

            TactileButton(
                onClick = onNavigateToJigs,
                modifier = Modifier.fillMaxWidth(),
                variant = TactileButtonVariant.PRIMARY,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                text = "Configure",
                testTag = "configure_jig_button"
            )
        }
    }
}

@Composable
private fun RodConfigDashboardCard(
    modifier: Modifier = Modifier,
    onNavigateToRods: () -> Unit
) {
    TactileCard(
        modifier = modifier
            .testTag("dashboard_rod_configurator_card")
            .clickable { onNavigateToRods() },
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFEA580C), Color(0xFFC2410C))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_rod_icon),
                        contentDescription = "Fishing Rod Icon",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFFEA580C),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = "Rods",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Text(
                    text = "Toray blanks & Fuji guides",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }

            TactileButton(
                onClick = onNavigateToRods,
                modifier = Modifier.fillMaxWidth(),
                variant = TactileButtonVariant.SECONDARY,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                text = "Configure",
                testTag = "configure_rod_button"
            )
        }
    }
}

@Composable
private fun LureConfigDashboardCard(
    modifier: Modifier = Modifier,
    onNavigateToLures: () -> Unit
) {
    TactileCard(
        modifier = modifier
            .testTag("dashboard_lure_configurator_card")
            .clickable { onNavigateToLures() },
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0D9488), Color(0xFF0F766E))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_lure_icon),
                        contentDescription = "Hard Lure Icon",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF0D9488),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = "Lures",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Text(
                    text = "Minnows, cranks & poppers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }

            TactileButton(
                onClick = onNavigateToLures,
                modifier = Modifier.fillMaxWidth(),
                variant = TactileButtonVariant.PRIMARY,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                text = "Configure",
                testTag = "configure_lure_button"
            )
        }
    }
}

@Composable
private fun PackagingConfigDashboardCard(
    modifier: Modifier = Modifier,
    onNavigateToPackaging: () -> Unit
) {
    TactileCard(
        modifier = modifier
            .testTag("dashboard_packaging_card")
            .clickable { onNavigateToPackaging() },
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF475569), Color(0xFF1E293B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_package_box),
                        contentDescription = "Packaging Box Icon",
                        modifier = Modifier.size(22.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = "Packaging",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Text(
                    text = "Retail boxes & blister packs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }

            TactileButton(
                onClick = onNavigateToPackaging,
                modifier = Modifier.fillMaxWidth(),
                variant = TactileButtonVariant.OUTLINE,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                text = "Configure",
                testTag = "configure_packaging_button"
            )
        }
    }
}
