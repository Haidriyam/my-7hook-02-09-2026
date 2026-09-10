package com.example.data.model

import java.security.MessageDigest
import java.util.Locale

/**
 * Canonical 7Hooks Jig Configuration model.
 * Produces deterministic canonical representations and SHA-256 hashes
 * for reproducible AI rendering, caching, and parametric CAD synchronization.
 */
data class JigConfiguration(
    val shapeId: String = "flutter",
    val shapeVersion: Int = 1,
    val weightGrams: Float = 80f,
    val lengthMm: Float = 130f,
    val widthMm: Float = 22f,
    val mainColor: String = "Tournament Orange",
    val mainColorHex: Long = 0xFFEA580C,
    val hasDualTone: Boolean = true,
    val secondaryColor: String = "Stealth Black",
    val secondaryColorHex: Long = 0xFF0F172A,
    val pattern: String = "Tiger",
    val patternColor: String = "Stealth Black",
    val patternColorHex: Long = 0xFF0F172A,
    val finish: String = "Metallic",
    val material: String = "Lead-Free Zinc Alloy",
    val eyeStyle: String = "3D Strike",
    val eyeColor: String = "Ruby Red",
    val eyeSize: String = "Medium (8mm)",
    val hookType: String = "O'Shaughnessy",
    val hookSize: String = "3/0",
    val assistHook: String = "Standard Mustad 3/0",
    val assistCord: String = "Braided PE",
    val assistCordColor: String = "Red",
    val frontRing: String = "Standard",
    val backRing: String = "Heavy Duty",
    val topRing: String = "None",
    val bottomRing: String = "None",
    val ringSize: String = "#5 (5.5mm)",
    val colorComboName: String = "",
    val customValues: Map<String, String> = emptyMap(),
    val referenceNumber: String = ""
) {

    /**
     * Deterministic canonical key-value representation with strictly sorted keys,
     * normalized numeric formatting, and uppercase standard identifiers.
     */
    fun toCanonicalString(promptVersion: Int = 1, modelVersion: String = "gemini-2.5-flash-image"): String {
        val sortedPairs = sortedMapOf(
            "assistCord" to assistCord.trim().uppercase(Locale.US),
            "assistCordColor" to assistCordColor.trim().uppercase(Locale.US),
            "assistHook" to assistHook.trim().uppercase(Locale.US),
            "backRing" to backRing.trim().uppercase(Locale.US),
            "bottomRing" to bottomRing.trim().uppercase(Locale.US),
            "custom" to customValues.entries.sortedBy { it.key }.joinToString(";") { "${it.key.uppercase(Locale.US)}=${it.value.uppercase(Locale.US)}" },
            "eyeColor" to eyeColor.trim().uppercase(Locale.US),
            "eyeSize" to eyeSize.trim().uppercase(Locale.US),
            "eyeStyle" to eyeStyle.trim().uppercase(Locale.US),
            "finish" to finish.trim().uppercase(Locale.US),
            "frontRing" to frontRing.trim().uppercase(Locale.US),
            "hasDualTone" to hasDualTone.toString(),
            "hookSize" to hookSize.trim().uppercase(Locale.US),
            "hookType" to hookType.trim().uppercase(Locale.US),
            "length" to String.format(Locale.US, "%.1f", lengthMm),
            "mainColor" to mainColor.trim().uppercase(Locale.US),
            "material" to material.trim().uppercase(Locale.US),
            "modelVersion" to modelVersion.trim().uppercase(Locale.US),
            "pattern" to pattern.trim().uppercase(Locale.US),
            "patternColor" to patternColor.trim().uppercase(Locale.US),
            "promptVersion" to promptVersion.toString(),
            "ringSize" to ringSize.trim().uppercase(Locale.US),
            "secondaryColor" to (if (hasDualTone) secondaryColor.trim().uppercase(Locale.US) else "NONE"),
            "shape" to shapeId.trim().uppercase(Locale.US),
            "shapeVersion" to shapeVersion.toString(),
            "topRing" to topRing.trim().uppercase(Locale.US),
            "weight" to String.format(Locale.US, "%.1f", weightGrams),
            "width" to String.format(Locale.US, "%.1f", widthMm)
        )

        return sortedPairs.entries.joinToString("&") { "${it.key}=${it.value}" }
    }

    /**
     * Deterministic SHA-256 identity hash for the exact configured product.
     */
    val configurationHash: String
        get() = sha256(toCanonicalString())

    /**
     * Unique cache render key accounting for model & prompt versioning.
     */
    fun getRenderKey(promptVersion: Int = 1, modelVersion: String = "gemini-2.5-flash-image"): String {
        return sha256(toCanonicalString(promptVersion, modelVersion))
    }

    /**
     * Converts to the shared [ProductConfiguration] used by technical drawings & PDF generator.
     */
    fun toProductConfiguration(imageUrl: String = ""): ProductConfiguration {
        val shape = JigShapeRepository.getById(shapeId)
        val ref = referenceNumber.ifEmpty { ProductConfiguration.generateReferenceNumber(ConfigType.JIG) }
        val model = "7H-JIG-${shape.shapeName.take(3).uppercase(Locale.US)}-${weightGrams.toInt()}G"

        return ProductConfiguration(
            configId = "CFG-${configurationHash.take(10)}",
            referenceNumber = ref,
            configType = ConfigType.JIG,
            productId = "jig_${shape.shapeId}",
            productName = "${shape.shapeName} Jig (${mainColor})",
            modelNumber = model,
            category = shape.bodyProfile,
            imageUrl = imageUrl,
            weightGrams = weightGrams,
            lengthMm = lengthMm,
            widthMm = widthMm,
            material = customValues["material"] ?: "Marine Grade Alloy",
            colorName = "$mainColor / $secondaryColor",
            baseColorHex = mainColorHex,
            accentColorHex = secondaryColorHex,
            patternType = when (pattern.lowercase(Locale.US)) {
                "dots" -> JigPatternType.DOT_PATTERN
                "scales" -> JigPatternType.CRYSTAL_FACET
                else -> JigPatternType.SOLID_STRIPE
            },
            frontRing = frontRing,
            backRing = backRing,
            hookTypeJig = assistHook,
            assistCord = assistCordColor,
            threadColor = assistCordColor,
            threadColorHex = null,
            finishType = finish,
            eyeStyle = eyeStyle,
            customFrontRing = customValues["frontRing"] ?: "",
            customBackRing = customValues["backRing"] ?: "",
            customHook = customValues["assistHook"] ?: "",
            customAssistCordColor = customValues["assistCord"] ?: "",
            customFinish = customValues["finish"] ?: "",
            customWeight = customValues["weight"] ?: "",
            customLength = customValues["length"] ?: "",
            customWidth = customValues["width"] ?: ""
        )
    }

    companion object {
        fun fromShape(shape: JigShapeTemplate): JigConfiguration {
            return JigConfiguration(
                shapeId = shape.shapeId,
                weightGrams = shape.defaultWeightGrams,
                lengthMm = shape.defaultLengthMm,
                widthMm = shape.defaultWidthMm,
                mainColor = shape.defaultMainColor,
                mainColorHex = shape.defaultMainColorHex,
                secondaryColor = shape.defaultSecondaryColor,
                secondaryColorHex = shape.defaultSecondaryColorHex,
                pattern = shape.defaultPattern,
                patternColor = shape.defaultPatternColor,
                patternColorHex = shape.defaultPatternColorHex,
                finish = shape.defaultFinish,
                eyeStyle = shape.defaultEyeStyle,
                eyeColor = shape.defaultEyeColor,
                assistHook = shape.defaultAssistHook,
                assistCordColor = shape.defaultAssistCordColor,
                frontRing = shape.defaultFrontRing,
                backRing = shape.defaultBackRing
            )
        }

        fun sha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
