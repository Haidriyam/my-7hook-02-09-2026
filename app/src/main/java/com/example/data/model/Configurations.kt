package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ConfigType {
    JIG,
    ROD,
    LURE
}

data class ProductConfiguration(
    val configId: String = generateConfigId(),
    val referenceNumber: String = generateReferenceNumber(ConfigType.JIG),
    val configType: ConfigType = ConfigType.JIG,
    val productId: String = "",
    val productName: String = "",
    val modelNumber: String = "",
    val category: String = "",
    // Dimensions
    val weightGrams: Float = 40f,
    val lengthMm: Float = 110f,
    val widthMm: Float = 22f, // or blank diameter for rods
    val heightMm: Float = 14f, // thickness / depth
    // Specs & Appearance
    val material: String = "Stainless Steel",
    val shape: String = "Football",
    val colorName: String = "Orange-Black",
    val baseColorHex: Long = 0xFFEA580C,
    val accentColorHex: Long = 0xFF0F172A,
    val hasAccentColor: Boolean = true,
    val finishType: String = "Holographic",
    val patternType: JigPatternType = JigPatternType.SOLID_STRIPE,
    val patternName: String = "Stripes",
    // Eye Configuration
    val eyeStyle: String = "3D",
    val eyeShape: String = "Round",
    val eyeColorHex: Long = 0xFFFFD700,
    // Hardware Configuration
    val hookLevel: String = "Standard",
    val hookStyle: String = "O'Shaughnessy",
    val hookSize: String = "2/0",
    val hookQuantity: Int = 1,
    val additionalComponent: String = "None",
    // Special Features
    val hasGlow: Boolean = false,
    val hasUvReactive: Boolean = false,
    val hasRattle: Boolean = false,
    val hasWeedGuard: Boolean = false,
    val customMarking: String = "",
    // Rod Specifics
    val rodType: String = "Spinning Rod",
    val power: String = "Medium",
    val action: String = "Fast",
    val sections: Int = 2,
    val recommendedLineWeight: String = "12 - 25 lb (PE 1.5 - 3.0)",
    val recommendedLureWeight: String = "15 - 60 g",
    val maximumLoadKg: Float = 12f,
    val handleLengthMm: Float = 420f,
    // Lure Specifics
    val divingDepthMeters: Float = 1.8f,
    val hookType: String = "VMC Saltwater 3X Treble",
    val buoyancy: String = "Suspending",
    // Manufacturing & Technical Drawing Metadata
    val revision: String = "A",
    val drawingStatus: String = "FOR REVIEW",
    val drawingNumber: String = "",
    val generalTolerance: String = "TBD",
    val weightTolerance: String = "TBD",
    val densityGrade: String = "Standard Production Alloy",
    val datumA: String = "Primary Centerline X-X",
    val datumB: String = "Head Reference Plane Y-Y",
    val coating: String = "UV Marine Clear Coat",
    val coatingThickness: String = "0.08 mm",
    val notes: String = "Standard precision manufacturing tolerances apply. ISO 9001 certified finish.",
    val timestamp: Long = System.currentTimeMillis()
) {
    val effectiveDrawingNumber: String
        get() = if (drawingNumber.isNotBlank()) drawingNumber else {
            when (configType) {
                ConfigType.JIG -> "7H-JIG-${modelNumber.ifBlank { "0001" }}"
                ConfigType.ROD -> "7H-ROD-${modelNumber.ifBlank { "0001" }}"
                ConfigType.LURE -> "7H-LUR-${modelNumber.ifBlank { "0001" }}"
            }
        }
    companion object {
        fun generateConfigId(): String = "CFG-${System.currentTimeMillis()}"
        
        fun generateReferenceNumber(type: ConfigType): String {
            val year = SimpleDateFormat("yyyy", Locale.US).format(Date())
            val seq = (1000..9999).random()
            return when (type) {
                ConfigType.JIG -> "7H-JIG-$year-$seq"
                ConfigType.ROD -> "7H-ROD-$year-$seq"
                ConfigType.LURE -> "7H-LUR-$year-$seq"
            }
        }
    }
}

data class PackagingConfiguration(
    val packagingId: String = "PKG-${System.currentTimeMillis()}",
    val referenceNumber: String = "7H-PKG-${SimpleDateFormat("yyyy", Locale.US).format(Date())}-${(1000..9999).random()}",
    val packagingType: String = "Box", // Box, Sleeve, Pouch, Blister, Custom
    val productName: String = "7Hooks Precision Tackle",
    val modelNumber: String = "7H-PKG-001",
    val companyName: String = "7Hooks Global Tackle",
    val customLogoUri: String? = null,
    val productDescription: String = "Engineered premium sports fishing tackle crafted for professional competition and demanding sea conditions.",
    val contactWebsite: String = "www.7hooks.com",
    val contactPhone: String = "+1 (800) 746-6571",
    val contactEmail: String = "engineering@7hooks.com",
    val packagingNotes: String = "FSC-certified recyclable packaging with UV spot varnished 7Hooks branding.",
    val packagingMaterial: String = "High-Density Cardboard 350gsm",
    val packagingColor: String = "Nautical Navy / Metallic Cyan",
    val packagingDimensions: String = "180 x 60 x 30 mm",
    val timestamp: Long = System.currentTimeMillis()
)

data class UserAccount(
    val email: String,
    val name: String,
    val company: String = "",
    val phone: String = "",
    val isLoggedIn: Boolean = true
)
