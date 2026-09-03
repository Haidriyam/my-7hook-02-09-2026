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
    // Specs
    val material: String = "Stainless Steel",
    val colorName: String = "Orange-Black",
    val baseColorHex: Long = 0xFFEA580C,
    val accentColorHex: Long = 0xFF0F172A,
    val patternType: JigPatternType = JigPatternType.SOLID_STRIPE,
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
    // Metadata
    val notes: String = "Standard precision manufacturing tolerances apply (±0.15mm). ISO 9001 certified finish.",
    val timestamp: Long = System.currentTimeMillis()
) {
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
