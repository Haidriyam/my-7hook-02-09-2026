package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ProductCatalog
import com.example.data.model.RodCategoryType
import com.example.data.model.RodLengthGroup
import com.example.data.model.RodProduct
import com.example.ui.components.*

@Composable
fun RodCategoryScreen(
    onSelectCategory: (RodCategoryType, RodLengthGroup) -> Unit,
    onNavigateBack: () -> Unit
) {
    val rodProducts = ProductCatalog.rods

    Scaffold(
        topBar = {
            AppHeader(
                title = "Rods",
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        modifier = Modifier.testTag("rod_category_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // STEP 1 OF 3 Guided Progress Indicator
            item {
                TactileStepIndicator(
                    currentStep = 1,
                    totalSteps = 3,
                    stepTitles = listOf("Choose Rod", "Technical Specs", "Technical Drawing"),
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
            }

            item {
                TactileCard(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "3 Professional Rod Disciplines",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Select an engineered rod discipline and initial blank length group to enter finite element load testing, carbon fiber layups, and technical drawing export.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(rodProducts, key = { it.id }) { rod ->
                RodProductCategoryCard(
                    rod = rod,
                    onConfigure = { lengthGroup ->
                        onSelectCategory(rod.category, lengthGroup)
                    }
                )
            }
        }
    }
}

@Composable
private fun RodProductCategoryCard(
    rod: RodProduct,
    onConfigure: (RodLengthGroup) -> Unit
) {
    var selectedLengthGroup by remember { mutableStateOf(RodLengthGroup.MEDIUM) }

    TactileCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rod_category_card_${rod.id}"),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Controlled Reference Image (Height ~140dp, ContentScale.Fit)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = rod.imageUrl,
                    contentDescription = rod.name,
                    contentScale = ContentScale.Fit,
                    error = painterResource(id = R.drawable.ic_rod_icon),
                    fallback = painterResource(id = R.drawable.ic_rod_icon),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )

                // Category badge top left
                Surface(
                    color = Color(0xFFEA580C),
                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = rod.category.displayName.uppercase(),
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = rod.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Model Series: ${rod.modelNumber} • ${rod.category.subtitle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = rod.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Length Group Segmented Controls
            Text(
                text = "LENGTH GROUP SELECTION:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RodLengthGroup.values().forEach { group ->
                    val isSelected = selectedLengthGroup == group
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { selectedLengthGroup = group }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = group.displayName,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TactileButton(
                onClick = { onConfigure(selectedLengthGroup) },
                modifier = Modifier.fillMaxWidth(),
                variant = TactileButtonVariant.SECONDARY,
                icon = Icons.Default.Tune,
                text = "Configure ${rod.category.displayName}",
                testTag = "configure_rod_button_${rod.id}"
            )
        }
    }
}
