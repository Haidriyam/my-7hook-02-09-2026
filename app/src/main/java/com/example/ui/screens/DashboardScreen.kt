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
                    Box(modifier = Modifier.height(36.dp)) {
                        AsyncImage(
                            model = "https://7hooks.com/wp-content/uploads/2025/11/cropped-394608127_1379560635982079_6192428464456395363_n-removebg-preview-e1764314943627.png",
                            contentDescription = "7Hooks Brand",
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            error = painterResource(id = R.drawable.ic_7hooks_logo),
                            fallback = painterResource(id = R.drawable.ic_7hooks_logo),
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentUser?.name ?: "Technical Configurator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentUser?.company?.ifEmpty { "OEM Fishing Division" } ?: "OEM Fishing Division",
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
                // Welcome Hero Header
                item {
                    TactileCard(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (currentUser != null) "Welcome, ${currentUser.name}" else "7Hooks Engineering Hub",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Precision CAD & Technical Customization Platform",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                ThemeSelectorRow()
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select a product category to configure custom technical specifications, view CAD drawings, and generate true A4 manufacturing specification PDFs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // PRIMARY CONFIGURATION OPTIONS
                item {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isWide = maxWidth >= 600.dp
                        if (isWide) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                JigConfigDashboardCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onNavigateToJigs = onNavigateToJigs
                                )
                                RodConfigDashboardCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onNavigateToRods = onNavigateToRods
                                )
                            }
                        }
                    }
                }

                // PACKAGING CONFIGURATOR QUICK ACCESS
                item {
                    TactileCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPackaging() }
                            .testTag("dashboard_packaging_card"),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(10.dp))
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
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PACKAGING CONFIGURATOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Design Custom Product Packaging",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Upload company logo • 2.5D Mockup • A4 Packaging PDF",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Packaging",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // SAVED CONFIGURATIONS SECTION
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SAVED CONFIGURATIONS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "${savedConfigs.size} saved blueprints",
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
                                    text = "Configure a Jig or Rod and tap 'Save' on the CAD screen to archive blueprints.",
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
                            if (configEntity.configType == "JIG") MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (configEntity.configType == "JIG") Icons.Default.PrecisionManufacturing else Icons.Default.Tune,
                        contentDescription = null,
                        tint = if (configEntity.configType == "JIG") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
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
                        text = "Specs: ${configEntity.lengthMm.toInt()}mm | ${configEntity.weightGrams.toInt()}g | ${configEntity.material}",
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
private fun JigConfigDashboardCard(
    modifier: Modifier = Modifier,
    onNavigateToJigs: () -> Unit
) {
    TactileCard(
        modifier = modifier
            .testTag("dashboard_jig_configurator_card"),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
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
                        modifier = Modifier.size(34.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "JIG CONFIGURATOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Many jig options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "Configure jig dimensions, materials, colors and technical specifications.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            TactileButton(
                onClick = onNavigateToJigs,
                modifier = Modifier.fillMaxWidth(),
                variant = TactileButtonVariant.PRIMARY,
                icon = Icons.Default.Tune,
                text = "Configure Jig",
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
            .testTag("dashboard_rod_configurator_card"),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
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
                        modifier = Modifier.size(34.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ROD CONFIGURATOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEA580C),
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Rod Configurator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "Configure rod length, materials, specifications and load characteristics.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            TactileButton(
                onClick = onNavigateToRods,
                modifier = Modifier.fillMaxWidth(),
                variant = TactileButtonVariant.SECONDARY,
                icon = Icons.Default.Tune,
                text = "Configure Rod",
                testTag = "configure_rod_button"
            )
        }
    }
}
