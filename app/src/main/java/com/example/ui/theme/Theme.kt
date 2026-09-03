package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Luxury Industrial Light Palette
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightSurfaceContainer = Color(0xFFF4F6F9)
val LightPrimary = Color(0xFF0284C7) // Precision Blue
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFE0F2FE)
val LightOnPrimaryContainer = Color(0xFF0369A1)
val LightSecondary = Color(0xFFEA580C) // Performance Blaze Orange
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFFEDD5)
val LightOnSecondaryContainer = Color(0xFF9A3412)
val LightTertiary = Color(0xFF0D9488) // Nautical Teal
val LightOnTertiary = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF0F172A) // Rich Charcoal Slate
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF475569) // Slate Gray
val LightOutline = Color(0xFFCBD5E1) // Soft Metallic Outline
val LightOutlineVariant = Color(0xFFE2E8F0)

// Luxury Industrial Dark Palette
val DarkBackground = Color(0xFF0B1120) // Deep Night Graphite
val DarkSurface = Color(0xFF1E293B) // Dark Slate
val DarkSurfaceVariant = Color(0xFF334155) // Metallic Charcoal
val DarkSurfaceContainer = Color(0xFF162032)
val DarkPrimary = Color(0xFF38BDF8) // Electric Cyan
val DarkOnPrimary = Color(0xFF082F49)
val DarkPrimaryContainer = Color(0xFF075985)
val DarkOnPrimaryContainer = Color(0xFFE0F2FE)
val DarkSecondary = Color(0xFFFB923C) // Warm Amber Orange
val DarkOnSecondary = Color(0xFF431407)
val DarkSecondaryContainer = Color(0xFF7C2D12)
val DarkOnSecondaryContainer = Color(0xFFFFEDD5)
val DarkTertiary = Color(0xFF2DD4BF)
val DarkOnTertiary = Color(0xFF042F2E)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF475569)
val DarkOutlineVariant = Color(0xFF334155)

// Metallic & Tactile Accent Constants
val MetallicHighlight = Color(0x33FFFFFF)
val MetallicShadow = Color(0x40000000)
val TechGridColorLight = Color(0x180284C7)
val TechGridColorDark = Color(0x2238BDF8)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

@Composable
fun SevenHooksTheme(
    themeMode: AppThemeMode = ThemeManager.currentThemeMode,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
