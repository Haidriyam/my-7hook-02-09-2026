package com.example.data.model

data class JigProduct(
    val id: String,
    val name: String,
    val category: String,
    val imageUrl: String,
    val baseColorHex: Long,
    val accentColorHex: Long,
    val defaultWeightGrams: Float = 40f,
    val minWeightGrams: Float = 10f,
    val maxWeightGrams: Float = 250f,
    val defaultLengthMm: Float = 110f,
    val minLengthMm: Float = 40f,
    val maxLengthMm: Float = 280f,
    val defaultWidthMm: Float = 22f,
    val minWidthMm: Float = 8f,
    val maxWidthMm: Float = 45f,
    val materials: List<String> = listOf("Stainless Steel", "Carbon Steel", "Aluminum", "Brass", "Tungsten Alloy", "Custom Alloy"),
    val modelNumber: String,
    val description: String,
    val isNew: Boolean = false,
    val patternType: JigPatternType = JigPatternType.SOLID_STRIPE,
    val availableFrontRings: List<String> = listOf("None", "Standard", "Heavy Duty"),
    val availableBackRings: List<String> = listOf("None", "Standard", "Heavy Duty"),
    val defaultFrontRing: String = "Standard",
    val defaultBackRing: String = "Standard",
    val defaultHook: String = "Mustad Saltwater Assist 3/0",
    val availableHooks: List<String> = listOf("None", "Mustad Saltwater Assist 3/0", "BKK Deep Sea Jig Hook", "Owner Monster Assist 5/0", "Twin Assist Rig 3/0", "Heavy Wire Tuna Hook"),
    val localDrawableRes: Int? = null
)

enum class JigPatternType(val displayName: String) {
    SOLID_STRIPE("Solid Stripe"),
    DOT_PATTERN("Dot Pattern"),
    HOLOGRAPHIC_SLASH("Holo Slash"),
    CRYSTAL_FACET("Crystal Facet")
}

data class LureProduct(
    val id: String,
    val name: String,
    val category: String,
    val imageUrl: String,
    val baseColorHex: Long,
    val accentColorHex: Long,
    val defaultWeightGrams: Float = 35f,
    val minWeightGrams: Float = 10f,
    val maxWeightGrams: Float = 120f,
    val defaultLengthMm: Float = 110f,
    val minLengthMm: Float = 50f,
    val maxLengthMm: Float = 220f,
    val defaultWidthMm: Float = 20f,
    val minWidthMm: Float = 10f,
    val maxWidthMm: Float = 36f,
    val materials: List<String> = listOf("ABS Engineered Resin", "Solid Balsa Core", "Polycarbonate", "Tungsten Weight Core", "Zinc Alloy"),
    val modelNumber: String,
    val description: String,
    val isNew: Boolean = false,
    val patternType: JigPatternType = JigPatternType.SOLID_STRIPE,
    val divingDepthMeters: Float = 1.8f,
    val minDivingDepthMeters: Float = 0.0f,
    val maxDivingDepthMeters: Float = 8.0f,
    val hookType: String = "VMC Saltwater 3X Treble",
    val buoyancy: String = "Suspending"
) {
    val defaultDivingDepthMeters: Float get() = divingDepthMeters
    val defaultHookType: String get() = hookType
    val defaultBuoyancy: String get() = buoyancy
}

enum class RodCategoryType(val displayName: String, val subtitle: String, val imageUrl: String) {
    SPINNING(
        displayName = "Spinning Rod",
        subtitle = "Precision casting & all-round versatility",
        imageUrl = "https://m.media-amazon.com/images/I/518S1uY+2qL._AC_UF1000,1000_QL80_.jpg"
    ),
    CASTING(
        displayName = "Casting Rod",
        subtitle = "Heavy lure power & trigger grip accuracy",
        imageUrl = "https://m.media-amazon.com/images/I/4160CgHpeIL._AC_UF1000,1000_QL80_.jpg"
    ),
    OFFSHORE_BOAT(
        displayName = "Offshore / Boat Rod",
        subtitle = "Deep sea jigging, trolling & maximum strength",
        imageUrl = "https://m.media-amazon.com/images/I/61m7tYflPgL._AC_UF1000,1000_QL80_.jpg"
    )
}

enum class RodLengthGroup(val displayName: String, val defaultLengthMm: Float, val minMm: Float, val maxMm: Float) {
    SHORT("Short (1.8m - 2.1m)", 1980f, 1800f, 2100f),
    MEDIUM("Medium (2.1m - 2.7m)", 2400f, 2100f, 2700f),
    FULL_LENGTH("Full Length (2.7m - 3.6m)", 3000f, 2700f, 3600f)
}

enum class PackagingType(val displayName: String, val subtitle: String) {
    CLAMSHELL_BLISTER("Clamshell / Blister Pack", "Thermoformed clear PET with heat-sealed backing card"),
    HEADER_CARD_POLYBAG("Header Card / Polybag", "Fold-over cardboard header with heavy-gauge polybag"),
    RETAIL_HANGING_BOX("Retail Hanging Box", "Die-cut tuck-end cardboard box with euro-slot tab"),
    RIGID_GIFT_BOX("Rigid Gift Box", "Heavy paperboard two-piece box with custom EVA foam insert"),
    BULK_OEM_PACK("Bulk OEM Pack", "Industrial master carton with compartment divider trays")
}

data class RodProduct(
    val id: String,
    val name: String,
    val category: RodCategoryType,
    val imageUrl: String,
    val modelNumber: String,
    val defaultLengthMm: Float = 2400f,
    val minLengthMm: Float = 1800f,
    val maxLengthMm: Float = 3600f,
    val defaultWeightGrams: Float = 165f,
    val minWeightGrams: Float = 80f,
    val maxWeightGrams: Float = 450f,
    val defaultBlankDiameterMm: Float = 12.5f,
    val minBlankDiameterMm: Float = 6f,
    val maxBlankDiameterMm: Float = 24f,
    val materials: List<String> = listOf("30T Toray Carbon", "40T High Modulus Carbon", "24T Carbon Fiber", "E-Glass Composite", "Carbon-Kevlar Hybrid"),
    val powers: List<String> = listOf("Ultra Light", "Light", "Medium Light", "Medium", "Medium Heavy", "Heavy", "Extra Heavy"),
    val actions: List<String> = listOf("Slow", "Moderate", "Moderate Fast", "Fast", "Extra Fast"),
    val sectionOptions: List<Int> = listOf(1, 2, 3, 4),
    val description: String
)
