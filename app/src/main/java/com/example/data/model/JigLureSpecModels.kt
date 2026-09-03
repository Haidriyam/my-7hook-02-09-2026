package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * 7Hooks Advanced Jig & Lure Engineering Specifications
 * Single source of truth for progressive configuration and manufacturing documentation.
 */

enum class JigShape(
    val displayName: String,
    val description: String,
    val categoryHint: String
) {
    FOOTBALL("Football", "Wide oval profile with anti-roll stability across rock and bottom structure", "Deep Structure"),
    ROUND("Round", "Classic versatile hydrodynamic ball head for general jigging", "All-Round"),
    ARKIE("Arkie", "Triangular wedge head engineered to slip through heavy timber and weeds", "Flipping & Cover"),
    SWIMBAIT("Swimbait", "Sleek bullet hydro-head designed for horizontal swimming retrieve", "Swimbait"),
    STAND_UP("Stand-Up", "Flat-bottomed keel head that holds hook upright at 45° off the seabed", "Bottom Contact"),
    FINESSE("Finesse", "Compact low-profile head for subtle presentations and clear water", "Finesse"),
    BULLET("Bullet", "Pointed cone nose for rapid vertical descent and punching grass mats", "Fast Sinking"),
    MUSHROOM("Mushroom", "Flat-faced mushroom head ideal for Midwest finesse and hover presentations", "Ned / Finesse"),
    DART("Dart", "Angular triangular keel that produces an erratic darting side-to-side flutter", "Erratic Darting")
}

enum class LureType(
    val displayName: String,
    val description: String,
    val defaultDepthMeters: Float
) {
    MINNOW("Minnow", "Slender baitfish profile with tight rolling flutter on steady retrieve", 1.5f),
    JERKBAIT("Jerkbait", "Suspended darting hard bait with aggressive twitch-and-pause response", 1.8f),
    CRANKBAIT("Crankbait", "Bulbous body with broad polycarbonate bib for heavy wobble and vibration", 3.2f),
    SPOON("Spoon", "Curved metallic stamped body with wide fluttering flash on retrieve", 4.0f),
    POPPER("Popper", "Cupped mouth surface plug that generates loud acoustic pops and water spray", 0.0f),
    PENCIL("Pencil", "Walk-the-dog topwater stickbait for surface thrashing presentation", 0.0f),
    VIBE("Vibe", "Lipless vibrating lure with high-frequency resonance and fast sink rate", 5.0f),
    SOFT_PLASTIC("Soft Plastic", "Flexible elastomer swimbody with high-action paddle tail", 2.0f),
    SHAD("Shad", "Deep-bellied shad profile mimicking forage baitfish in open water", 2.5f)
}

data class MaterialOption(
    val name: String,
    val description: String,
    val isRecommended: Boolean = false,
    val densityGPerCm3: Float = 11.34f
)

object JigMaterials {
    val options = listOf(
        MaterialOption("Tungsten", "Denser material that allows a compact body and high sensitivity.", isRecommended = true, densityGPerCm3 = 19.3f),
        MaterialOption("Lead", "Traditional cost-effective dense casting material.", densityGPerCm3 = 11.34f),
        MaterialOption("Bismuth", "Lead-free eco-friendly dense metal.", densityGPerCm3 = 9.78f),
        MaterialOption("Tin Alloy", "Lightweight lead-free alloy for slow fluttering presentation.", densityGPerCm3 = 7.31f),
        MaterialOption("Zinc", "Hard durable alloy with excellent corrosion resistance.", densityGPerCm3 = 7.14f)
    )
}

object LureMaterials {
    val options = listOf(
        MaterialOption("ABS Engineered Resin", "High-impact acoustic chamber material with uniform wall thickness.", isRecommended = true),
        MaterialOption("Solid Balsa Core", "Natural buoyant wood core offering responsive flutter and high floatation."),
        MaterialOption("Polycarbonate", "Bulletproof impact resistance designed for aggressive predatory strikes."),
        MaterialOption("Cast Zinc Alloy", "Dense die-cast metal body for heavy casting and deep vibration."),
        MaterialOption("Soft Elastomer", "Tear-resistant pliable formulation with lifelike movement.")
    )
}

data class ColorOption(
    val name: String,
    val hex: Long,
    val color: Color
)

object StudioColors {
    val primaryColors = listOf(
        ColorOption("Safety Orange", 0xFFEA580C, Color(0xFFEA580C)),
        ColorOption("Solar Yellow", 0xFFFACC15, Color(0xFFFACC15)),
        ColorOption("Candy Blue", 0xFF0284C7, Color(0xFF0284C7)),
        ColorOption("Pulse Pink", 0xFFEC4899, Color(0xFFEC4899)),
        ColorOption("Forest Green", 0xFF16A34A, Color(0xFF16A34A)),
        ColorOption("Blaze Red", 0xFFDC2626, Color(0xFFDC2626)),
        ColorOption("Stealth Black", 0xFF0F172A, Color(0xFF0F172A)),
        ColorOption("Arctic White", 0xFFF8FAFC, Color(0xFFF8FAFC)),
        ColorOption("Chrome Silver", 0xFF94A3B8, Color(0xFF94A3B8)),
        ColorOption("Metallic Gold", 0xFFEAB308, Color(0xFFEAB308))
    )

    val eyeColors = listOf(
        ColorOption("Metallic Gold", 0xFFFFD700, Color(0xFFFFD700)),
        ColorOption("Ruby Red", 0xFFDC2626, Color(0xFFDC2626)),
        ColorOption("Silver Pearl", 0xFFE2E8F0, Color(0xFFE2E8F0)),
        ColorOption("Chartreuse", 0xFFA3E635, Color(0xFFA3E635)),
        ColorOption("Amber Orange", 0xFFF97316, Color(0xFFF97316))
    )
}

enum class StudioFinish(
    val displayName: String,
    val description: String
) {
    SOLID("Solid", "High-opacity uniform marine lacquer finish"),
    METALLIC("Metallic", "Fine aluminum flake base with rich metallic luster"),
    MATTE("Matte", "Zero-glare diffuse surface texture for spooky fish"),
    GLOSS("Gloss", "Ultra-clear high-refraction protective top coat"),
    GLITTER("Glitter", "Reflective micro-glitter suspended in clear resin"),
    GLOW("Glow", "Phosphorescent photoluminescent coating that charges under light"),
    UV_REACTIVE("UV Reactive", "Fluoresces vividly under natural ultraviolet marine light"),
    HOLOGRAPHIC("Holographic", "Rainbow prism laser foil reflecting dynamic spectral shifts"),
    NATURAL_SCALE("Natural Scale", "Photorealistic laser-etched scale lattice")
}

enum class StudioPattern(
    val displayName: String,
    val description: String
) {
    NONE("None", "Smooth unpatterned base finish"),
    DOTS("Dots", "Contrasting predator attraction dot cluster"),
    STRIPES("Stripes", "Bold hydrodynamic vertical trigger bars"),
    CHEVRON("Chevron", "V-shaped aerodynamic strike orientation bars"),
    TIGER("Tiger", "Aggressive organic tiger predator stripes"),
    DIAMONDS("Diamonds", "Prismatic faceted diamond reflective mesh"),
    SCALES("Scales", "Micro-embossed natural baitfish lateral scales"),
    CUSTOM("Custom", "Custom factory laser etched branding motif")
}

enum class EyeStyle(val displayName: String) {
    STANDARD("Standard"),
    THREE_D("3D"),
    HOLOGRAPHIC("Holographic"),
    PRISMATIC("Prismatic"),
    DOME("Dome")
}

enum class EyeShape(val displayName: String) {
    ROUND("Round"),
    OVAL("Oval"),
    CAT_EYE("Cat-Eye")
}

enum class HookLevel(val displayName: String) {
    STANDARD("Standard"),
    HEAVY_DUTY("Heavy Duty"),
    CUSTOM("Custom")
}

enum class HookFamily(val displayName: String) {
    OSH_AUGHNESSY("O'Shaughnessy"),
    ABERDEEN("Aberdeen"),
    WIDE_GAP("Wide Gap"),
    EWG("Extra Wide Gap (EWG)"),
    OCTOPUS("Octopus"),
    TREBLE("3X Treble")
}

enum class AdditionalComponent(
    val displayName: String,
    val technicalSpec: String
) {
    NONE("None", "No secondary body dressing"),
    SILICONE("Silicone", "Multi-strand silicone skirt assembly — material: silicone elastomer (40 strands)"),
    RUBBER("Rubber", "Flexible elastomer tail component — material: vulcanized rubber"),
    FEATHER("Feather", "Natural hand-tied plumage dressing — material: select rooster hackle"),
    HAIR("Hair", "Genuine bucktail fiber dressing — material: natural northern deer hair"),
    SOFT_PLASTIC("Soft Plastic", "Elastomer trailer component — material: high-grade PVC plastisol")
}

object SpecValidation {
    data class ValidationIssue(
        val message: String,
        val recommendedValue: String? = null
    )

    fun validateJig(
        weightGrams: Float,
        lengthMm: Float,
        hookSize: String,
        minWeight: Float,
        maxWeight: Float
    ): ValidationIssue? {
        if (weightGrams < minWeight || weightGrams > maxWeight) {
            return ValidationIssue(
                message = "Target weight is outside standard production parameters for this model (${minWeight.toInt()}–${maxWeight.toInt()}g).",
                recommendedValue = "${minWeight.toInt()}–${maxWeight.toInt()}g"
            )
        }

        val hookNum = hookSizeToNumber(hookSize)
        if (lengthMm < 80f && hookNum >= 40) { // e.g. 4/0 or 5/0 on a tiny 70mm jig
            return ValidationIssue(
                message = "Hook size $hookSize may interfere with the balance of an ${lengthMm.toInt()}mm jig.",
                recommendedValue = "Recommended: #1 or 1/0 hook size"
            )
        }
        if (lengthMm > 180f && hookNum <= 10) { // #4 or #2 on a huge 220mm jig
            return ValidationIssue(
                message = "Hook size $hookSize is undersized for a heavy ${lengthMm.toInt()}mm jig.",
                recommendedValue = "Recommended: 3/0 to 5/0 hook size"
            )
        }

        return null
    }

    fun validateLure(
        lengthMm: Float,
        weightGrams: Float,
        hookQuantity: Int
    ): ValidationIssue? {
        if (lengthMm < 70f && hookQuantity > 2) {
            return ValidationIssue(
                message = "More than 2 treble hooks on a ${lengthMm.toInt()}mm lure will cause hook tangling.",
                recommendedValue = "Recommended: 1 or 2 hooks"
            )
        }
        return null
    }

    private fun hookSizeToNumber(size: String): Int {
        return when (size) {
            "#4" -> 4
            "#2" -> 6
            "#1" -> 8
            "1/0" -> 15
            "2/0" -> 25
            "3/0" -> 35
            "4/0" -> 45
            "5/0" -> 55
            else -> 20
        }
    }
}
