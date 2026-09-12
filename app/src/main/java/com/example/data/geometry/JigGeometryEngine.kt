package com.example.data.geometry

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductCatalog
import com.example.data.model.ProductConfiguration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * 7HOOKS PRECISION PARAMETRIC JIG GEOMETRY & TECHNICAL DRAWING ENGINE
 *
 * Implements ISO 128 / BS 8888 First-Angle Orthographic Projection,
 * parametric geometry generation directly from ProductConfiguration,
 * collision-aware dimensioning, dynamic legends, and title blocks.
 */
object JigGeometryEngine {

    enum class JigSilhouetteType {
        // Basic Geometric
        ROUND,
        CIRCULAR,
        OVAL,
        DISC,
        FLAT_DISC,
        TEARDROP,
        PEAR,
        EGG,
        BULLET,
        CONE,
        CYLINDER,
        TAPERED_CYLINDER,

        // Head / Jig Styles
        BALL_HEAD,
        FOOTBALL_HEAD,
        ARKIE_HEAD,
        ROUND_HEAD,
        FINESSE_HEAD,
        MUSHROOM_HEAD,
        DART_HEAD,
        STAND_UP_HEAD,
        SWIMBAIT_HEAD,
        SHAKY_HEAD,
        NED_HEAD,
        TUBE_HEAD,

        // Fishing-Oriented Body Shapes
        LONG_SHAD,
        SHORT_SHAD,
        MINNOW,
        BAITFISH,
        HERRING,
        SARDINE,
        NEEDLE,
        SLIM,
        WIDE_BODY,
        DEEP_BODY,
        PADDLE,
        BLADE,
        SPOON,
        LEAF,

        // Special / Distinctive Silhouettes
        DIAMOND,
        HEXAGON,
        TRIANGLE,
        TEAR_BLADE,
        HAMMER,
        DART_BLADE,
        CRESCENT,
        SPLIT_BODY,
        TWIN_PROFILE,
        ASYMMETRIC,

        // 7Hooks Signature Series
        ASYMMETRIC_HYDRO_KEEL, // Orange-Black Jigs (7H-JIG-OB-2026)
        SLOW_PITCH_DIAMOND,     // Yellow-Dotted Jigs (7H-JIG-YD-2026)
        VERTICAL_NEEDLE_NOSE,   // Yellow-Orange Jigs (7H-JIG-YO-2026)
        PELAGIC_S_CURVE,        // Candy Blue - Orange (7H-JIG-CBO-40G)
        SHORE_CAST_TEARDROP,    // Candy Pink - Green (7H-JIG-CPG-40G)
        STEPPED_HYDROFOIL,      // Candy Yellow - Black (7H-JIG-CYB-40G)
        CRYSTAL_FACETED         // Crystal Pink-Blue & Yellow-Blue
    }

    enum class EngineeringPerspective(val label: String) {
        ORTHOGRAPHIC("Full Assembly"),
        FRONT_ELEVATION("Front View"),
        TOP_PLAN("Top View"),
        ISOMETRIC("3D Axonometric");

        val displayName: String get() = label
    }

    enum class EngineeringTheme(val label: String) {
        TECHNICAL_PAPER("Drafting Paper"),
        BLUEPRINT_NAVY("Blueprint Navy"),
        STUDIO_LIGHT("Studio Clean");

        val displayName: String get() = label
    }

    /**
     * Resolves the authentic physical silhouette type for a given product ID / model number.
     */
    fun resolveSilhouetteType(config: ProductConfiguration): JigSilhouetteType {
        val id = config.productId.lowercase().removePrefix("jig_").trim()
        val name = config.productName.lowercase().trim()
        val model = config.modelNumber.lowercase().trim()

        // 1. Direct template match from repository
        val template = com.example.data.model.JigShapeRepository.shapes.find {
            it.shapeId.equals(id, ignoreCase = true) ||
            name.startsWith(it.shapeName.lowercase(), ignoreCase = true) ||
            name.contains(it.shapeName.lowercase(), ignoreCase = true) ||
            it.shapeName.equals(name, ignoreCase = true) ||
            id == it.shapeId ||
            id.contains(it.shapeId, ignoreCase = true) ||
            model.contains(it.shapeId, ignoreCase = true)
        }
        if (template != null) {
            return template.silhouetteType
        }

        // 2. Keyword-based matching for specific silhouette families
        return when {
            // Basic Geometric
            id.contains("round_head") || name.contains("round head") -> JigSilhouetteType.ROUND_HEAD
            id.contains("flat_disc") || name.contains("flat disc") -> JigSilhouetteType.FLAT_DISC
            id.contains("round") || name.contains("round") -> JigSilhouetteType.ROUND
            id.contains("circular") || name.contains("circular") -> JigSilhouetteType.CIRCULAR
            id.contains("oval") || name.contains("oval") -> JigSilhouetteType.OVAL
            id.contains("disc") || name.contains("disc") -> JigSilhouetteType.DISC
            id.contains("teardrop") || name.contains("teardrop") -> JigSilhouetteType.TEARDROP
            id.contains("pear") || name.contains("pear") -> JigSilhouetteType.PEAR
            id.contains("egg") || name.contains("egg") -> JigSilhouetteType.EGG
            id.contains("bullet") || name.contains("bullet") -> JigSilhouetteType.BULLET
            id.contains("cone") || name.contains("cone") -> JigSilhouetteType.CONE
            id.contains("tapered") || name.contains("tapered cylinder") -> JigSilhouetteType.TAPERED_CYLINDER
            id.contains("cylinder") || name.contains("cylinder") -> JigSilhouetteType.CYLINDER

            // Head / Jig Styles
            id.contains("football") || name.contains("football") -> JigSilhouetteType.FOOTBALL_HEAD
            id.contains("arkie") || name.contains("arkie") -> JigSilhouetteType.ARKIE_HEAD
            id.contains("finesse") || name.contains("finesse") -> JigSilhouetteType.FINESSE_HEAD
            id.contains("mushroom") || name.contains("mushroom") -> JigSilhouetteType.MUSHROOM_HEAD
            id.contains("dart_blade") || name.contains("dart blade") -> JigSilhouetteType.DART_BLADE
            id.contains("dart") || name.contains("dart") -> JigSilhouetteType.DART_HEAD
            id.contains("stand_up") || name.contains("stand-up") || name.contains("stand up") -> JigSilhouetteType.STAND_UP_HEAD
            id.contains("swimbait") || name.contains("swimbait") -> JigSilhouetteType.SWIMBAIT_HEAD
            id.contains("shaky") || name.contains("shaky") -> JigSilhouetteType.SHAKY_HEAD
            id.contains("ned") || name.contains("ned") -> JigSilhouetteType.NED_HEAD
            id.contains("tube") || name.contains("tube") -> JigSilhouetteType.TUBE_HEAD
            id.contains("ball") || name.contains("ball") -> JigSilhouetteType.BALL_HEAD

            // Fishing-Oriented Body Shapes
            id.contains("long_shad") || name.contains("long shad") -> JigSilhouetteType.LONG_SHAD
            id.contains("short_shad") || name.contains("short shad") -> JigSilhouetteType.SHORT_SHAD
            id.contains("minnow") || name.contains("minnow") -> JigSilhouetteType.MINNOW
            id.contains("baitfish") || name.contains("baitfish") -> JigSilhouetteType.BAITFISH
            id.contains("herring") || name.contains("herring") -> JigSilhouetteType.HERRING
            id.contains("sardine") || name.contains("sardine") -> JigSilhouetteType.SARDINE
            id.contains("needle") || name.contains("needle") -> JigSilhouetteType.NEEDLE
            id.contains("slim") || name.contains("slim") -> JigSilhouetteType.SLIM
            id.contains("wide") || name.contains("wide body") -> JigSilhouetteType.WIDE_BODY
            id.contains("deep") || name.contains("deep body") -> JigSilhouetteType.DEEP_BODY
            id.contains("paddle") || name.contains("paddle") -> JigSilhouetteType.PADDLE
            id.contains("tear_blade") || name.contains("tear blade") -> JigSilhouetteType.TEAR_BLADE
            id.contains("blade") || name.contains("blade") -> JigSilhouetteType.BLADE
            id.contains("spoon") || name.contains("spoon") -> JigSilhouetteType.SPOON
            id.contains("leaf") || name.contains("leaf") -> JigSilhouetteType.LEAF

            // Special / Distinctive Silhouettes
            id.contains("diamond") || name.contains("diamond") -> JigSilhouetteType.DIAMOND
            id.contains("hexagon") || name.contains("hexagon") -> JigSilhouetteType.HEXAGON
            id.contains("triangle") || name.contains("triangle") -> JigSilhouetteType.TRIANGLE
            id.contains("hammer") || name.contains("hammer") -> JigSilhouetteType.HAMMER
            id.contains("crescent") || name.contains("crescent") -> JigSilhouetteType.CRESCENT
            id.contains("split") || name.contains("split body") -> JigSilhouetteType.SPLIT_BODY
            id.contains("twin") || name.contains("twin profile") -> JigSilhouetteType.TWIN_PROFILE

            // 7Hooks Signature Series
            id.contains("flutter") || name.contains("flutter") || id.contains("candy_blue_orange") || name.contains("candy blue") || model.contains("cbo") || model.contains("flu") ->
                JigSilhouetteType.PELAGIC_S_CURVE
            id.contains("knife") || name.contains("knife") || id.contains("yellow_orange") || name.contains("yellow-orange") || model.contains("yo") || model.contains("kni") ->
                JigSilhouetteType.VERTICAL_NEEDLE_NOSE
            id.contains("yellow_dotted") || name.contains("yellow-dotted") || model.contains("yd") ->
                JigSilhouetteType.SLOW_PITCH_DIAMOND
            id.contains("asymmetric") || name.contains("asymmetric") || id.contains("orange_black") || name.contains("orange-black") || model.contains("ob") || model.contains("asy") ->
                JigSilhouetteType.ASYMMETRIC_HYDRO_KEEL
            id.contains("stepped") || name.contains("stepped") || id.contains("candy_yellow_black") || name.contains("bumble") || model.contains("cyb-40g") || model.contains("ste") ->
                JigSilhouetteType.STEPPED_HYDROFOIL
            id.contains("shore") || name.contains("shore") || id.contains("candy_pink_green") || name.contains("candy pink") || model.contains("cpg") || model.contains("sho") ->
                JigSilhouetteType.SHORE_CAST_TEARDROP
            id.contains("crystal") || name.contains("crystal") || model.contains("cpb") || model.contains("cyb-05") ->
                JigSilhouetteType.CRYSTAL_FACETED

            else -> JigSilhouetteType.ASYMMETRIC_HYDRO_KEEL
        }
    }

    /**
     * Color resolution helper for assist cord / thread.
     */
    fun resolveAssistCordColor(config: ProductConfiguration): Int {
        if (config.threadColorHex != null) {
            return config.threadColorHex.toInt()
        }
        val lower = config.threadColor.lowercase().trim()
        val customLower = config.customAssistCordColor.lowercase().trim()
        val matchStr = if (lower == "custom") customLower else lower

        return when {
            matchStr.contains("blue") -> Color.rgb(2, 132, 199)
            matchStr.contains("red") -> Color.rgb(220, 38, 38)
            matchStr.contains("orange") -> Color.rgb(234, 88, 12)
            matchStr.contains("yellow") || matchStr.contains("gold") -> Color.rgb(234, 179, 8)
            matchStr.contains("green") -> Color.rgb(22, 163, 74)
            matchStr.contains("black") -> Color.rgb(30, 41, 59)
            matchStr.contains("white") -> Color.rgb(248, 250, 252)
            matchStr.contains("pink") -> Color.rgb(236, 72, 153)
            matchStr.contains("purple") -> Color.rgb(147, 51, 234)
            matchStr.contains("silver") -> Color.rgb(203, 213, 225)
            else -> Color.rgb(220, 38, 38) // Default red assist cord
        }
    }

    /**
     * Data bundle holding resolved parametric drawing values to ensure single source of truth.
     */
    data class JigDrawingParameters(
        val productName: String,
        val modelNumber: String,
        val weightGrams: Float,
        val lengthMm: Float,
        val widthMm: Float,
        val material: String,
        val finish: String,
        val mainColor: String,
        val secondColor: String,
        val frontRing: String,
        val backRing: String,
        val assistHook: String,
        val assistCord: String,
        val assistCordColorInt: Int,
        val silhouette: JigSilhouetteType,
        val hasFrontRing: Boolean,
        val hasBackRing: Boolean,
        val hasAssistHook: Boolean,
        val hasAssistCord: Boolean,
        val isFrontRingHeavyDuty: Boolean,
        val isBackRingHeavyDuty: Boolean,
        val drawingRefNumber: String,
        val baseColorInt: Int,
        val accentColorInt: Int
    )

    fun resolveParameters(config: ProductConfiguration): JigDrawingParameters {
        val matchedJig = ProductCatalog.jigs.find { it.id == config.productId } ?: ProductCatalog.jigs.first()
        val silhouette = resolveSilhouetteType(config)

        val hasFR = config.frontRing != "None"
        val hasBR = config.backRing != "None"
        val hasHook = config.hookTypeJig != "None"
        val hasCord = hasHook && config.threadColor != "None"

        val frLabel = when {
            config.frontRing == "Custom" && config.customFrontRing.isNotEmpty() -> "Custom (${config.customFrontRing})"
            else -> config.frontRing
        }

        val brLabel = when {
            config.backRing == "Custom" && config.customBackRing.isNotEmpty() -> "Custom (${config.customBackRing})"
            else -> config.backRing
        }

        val hookLabel = when {
            config.hookTypeJig == "Custom" && config.customHook.isNotEmpty() -> "Custom (${config.customHook})"
            else -> config.hookTypeJig
        }

        val cordLabel = when {
            config.threadColor == "Custom" && config.customAssistCordColor.isNotEmpty() -> "Custom (${config.customAssistCordColor})"
            config.threadColor == "None" -> "None"
            else -> config.threadColor
        }

        val finishLabel = when {
            config.finishType == "Custom" && config.customFinish.isNotEmpty() -> config.customFinish
            else -> config.finishType
        }

        // Main & Second Color resolution
        val mainCol = config.colorName.split("-", "/", "&").firstOrNull()?.trim() ?: config.colorName
        val secondCol = config.colorName.split("-", "/", "&").getOrNull(1)?.trim() ?: "Black"

        return JigDrawingParameters(
            productName = config.productName.ifEmpty { matchedJig.name },
            modelNumber = config.modelNumber.ifEmpty { matchedJig.modelNumber },
            weightGrams = config.weightGrams,
            lengthMm = config.lengthMm,
            widthMm = config.widthMm,
            material = config.material.ifEmpty { "Stainless Steel" },
            finish = finishLabel,
            mainColor = mainCol,
            secondColor = secondCol,
            frontRing = frLabel,
            backRing = brLabel,
            assistHook = hookLabel,
            assistCord = cordLabel,
            assistCordColorInt = resolveAssistCordColor(config),
            silhouette = silhouette,
            hasFrontRing = hasFR,
            hasBackRing = hasBR,
            hasAssistHook = hasHook,
            hasAssistCord = hasCord,
            isFrontRingHeavyDuty = config.frontRing.contains("Heavy Duty", ignoreCase = true),
            isBackRingHeavyDuty = config.backRing.contains("Heavy Duty", ignoreCase = true),
            drawingRefNumber = config.referenceNumber,
            baseColorInt = config.baseColorHex.toInt(),
            accentColorInt = config.accentColorHex.toInt()
        )
    }

    // =========================================================================
    // ANDROID CANVAS (PDF) RENDERING PIPELINE
    // =========================================================================

    /**
     * Renders the complete First-Angle Orthographic Jig Drawing into an Android Canvas (used for A4 PDF).
     */
    fun drawJigOrthographicPdf(
        canvas: Canvas,
        boxX: Float,
        boxY: Float,
        boxWidth: Float,
        boxHeight: Float,
        config: ProductConfiguration,
        perspective: EngineeringPerspective = EngineeringPerspective.ORTHOGRAPHIC,
        theme: EngineeringTheme = EngineeringTheme.TECHNICAL_PAPER,
        showDimensions: Boolean = true,
        showGrid: Boolean = true
    ) {
        val params = resolveParameters(config)

        val isDark = theme == EngineeringTheme.BLUEPRINT_NAVY
        val bgColor = when (theme) {
            EngineeringTheme.BLUEPRINT_NAVY -> Color.rgb(10, 25, 47)
            EngineeringTheme.STUDIO_LIGHT -> Color.rgb(255, 255, 255)
            EngineeringTheme.TECHNICAL_PAPER -> Color.rgb(252, 253, 255)
        }
        val borderColor = if (isDark) Color.rgb(30, 58, 138) else Color.rgb(203, 213, 225)
        val gridColor = if (isDark) Color.rgb(17, 34, 64) else Color.rgb(241, 245, 249)
        val primaryStroke = if (isDark) Color.rgb(226, 232, 240) else Color.rgb(15, 23, 42)
        val viewLabelColor = if (isDark) Color.rgb(147, 197, 253) else Color.rgb(15, 23, 42)
        val dimColor = if (isDark) Color.rgb(56, 189, 248) else Color.rgb(2, 132, 199)
        val breakColor = if (isDark) Color.rgb(148, 163, 184) else Color.rgb(71, 85, 105)

        // 1. Drawing Frame & Drafting Border
        val framePaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val frameFill = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, frameFill)
        canvas.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, framePaint)

        // Subtle drafting grid
        if (showGrid) {
            val gridPaint = Paint().apply {
                color = gridColor
                strokeWidth = 0.5f
                isAntiAlias = true
            }
            var gx = boxX + 20f
            while (gx < boxX + boxWidth) {
                canvas.drawLine(gx, boxY, gx, boxY + boxHeight, gridPaint)
                gx += 20f
            }
            var gy = boxY + 20f
            while (gy < boxY + boxHeight) {
                canvas.drawLine(boxX, gy, boxX + boxWidth, gy, gridPaint)
                gy += 20f
            }
        }

        // First-Angle Projection Symbol (Top-Right inside frame)
        if (perspective == EngineeringPerspective.ORTHOGRAPHIC) {
            drawFirstAngleProjectionSymbol(canvas, boxX + boxWidth - 110f, boxY + 12f)
        }

        // Parametric scaling based strictly on configuration
        // L scale: reference 130mm
        val normL = (params.lengthMm / 130f).coerceIn(0.60f, 1.40f)
        val normW = (params.widthMm / 22f).coerceIn(0.60f, 1.40f)

        val drawL = when (perspective) {
            EngineeringPerspective.ORTHOGRAPHIC -> 190f * normL
            EngineeringPerspective.FRONT_ELEVATION,
            EngineeringPerspective.TOP_PLAN -> 250f * normL
            EngineeringPerspective.ISOMETRIC -> 220f * normL
        }
        val drawW = when (perspective) {
            EngineeringPerspective.ORTHOGRAPHIC -> 32f * normW
            EngineeringPerspective.FRONT_ELEVATION,
            EngineeringPerspective.TOP_PLAN -> 44f * normW
            EngineeringPerspective.ISOMETRIC -> 38f * normW
        }
        val halfL = drawL / 2f
        val halfW = drawW / 2f

        // View Positioning based on selected perspective
        val isSingleView = perspective != EngineeringPerspective.ORTHOGRAPHIC
        val viewCenterX = if (isSingleView) boxX + boxWidth * 0.48f else boxX + 220f
        val frontY = if (isSingleView) boxY + boxHeight * 0.44f else boxY + 80f
        val topY = if (isSingleView) boxY + boxHeight * 0.44f else boxY + 185f
        val endCenterX = boxX + boxWidth - 85f
        val endCenterY = frontY
        val isoCenterX = if (isSingleView) boxX + boxWidth * 0.48f else boxX + boxWidth - 85f
        val isoCenterY = if (isSingleView) boxY + boxHeight * 0.44f else topY

        // Paints for standard technical drawing line hierarchy (Section 27)
        val outlinePaint = Paint().apply {
            color = primaryStroke
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            isAntiAlias = true
        }
        val centerLinePaint = Paint().apply {
            color = if (isDark) Color.rgb(248, 113, 113) else Color.rgb(220, 38, 38)
            strokeWidth = 0.75f
            pathEffect = DashPathEffect(floatArrayOf(10f, 3f, 2f, 3f), 0f)
            isAntiAlias = true
        }
        val breakLinePaint = Paint().apply {
            color = breakColor
            style = Paint.Style.STROKE
            strokeWidth = 1.0f
            isAntiAlias = true
        }
        val hiddenLinePaint = Paint().apply {
            color = if (isDark) Color.rgb(100, 116, 139) else Color.rgb(148, 163, 184)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            pathEffect = DashPathEffect(floatArrayOf(4f, 3f), 0f)
            isAntiAlias = true
        }
        val dimLinePaint = Paint().apply {
            color = dimColor
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        val dimTextPaint = Paint().apply {
            color = dimColor
            textSize = 7.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        val viewLabelPaint = Paint().apply {
            color = viewLabelColor
            textSize = 7.8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.05f
        }

        // Subtle gradient fill reflecting configured main/accent color
        val fillPaint = Paint().apply {
            shader = android.graphics.LinearGradient(
                viewCenterX, frontY - halfW,
                viewCenterX, frontY + halfW,
                params.baseColorInt,
                params.accentColorInt,
                android.graphics.Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
            alpha = 180
            isAntiAlias = true
        }

        // Base Line-Tie Eyelets (Front & Rear solid rings permanently part of the jig)
        val eyeletFill = Paint().apply { color = Color.rgb(226, 232, 240); style = Paint.Style.FILL; isAntiAlias = true }
        val eyeletStroke = Paint().apply { color = Color.rgb(100, 116, 139); style = Paint.Style.STROKE; strokeWidth = 1.2f; isAntiAlias = true }

        // =====================================================================
        // VIEW 1: FRONT ELEVATION (PRIMARY PROFILE)
        // =====================================================================
        if (perspective == EngineeringPerspective.ORTHOGRAPHIC || perspective == EngineeringPerspective.FRONT_ELEVATION) {
            val frontTitle = if (perspective == EngineeringPerspective.FRONT_ELEVATION) "FRONT ELEVATION (SCALE 1.3:1)" else "FRONT ELEVATION (SCALE 1:1 @ A4)"
            canvas.drawText(frontTitle, boxX + 16f, frontY - halfW - 24f, viewLabelPaint)

        // Centerline extending beyond geometry
        canvas.drawLine(viewCenterX - halfL - 25f, frontY, viewCenterX + halfL + 25f, frontY, centerLinePaint)

        // Construct Parametric Front Path based on authentic Jig silhouette
        val frontPath = buildFrontSilhouettePath(params.silhouette, viewCenterX, frontY, halfL, halfW)

        canvas.drawPath(frontPath, fillPaint)
        canvas.drawPath(frontPath, outlinePaint)

        // Distinctive Jig Break-Lines / Keel Spine / Facets (Silhouette-Accurate)
        drawSilhouetteFeaturesPdf(canvas, params.silhouette, viewCenterX, frontY, halfL, halfW, breakLinePaint)

        canvas.drawCircle(viewCenterX - halfL - 5f, frontY, 3.8f, eyeletFill)
        canvas.drawCircle(viewCenterX - halfL - 5f, frontY, 3.8f, eyeletStroke)
        canvas.drawCircle(viewCenterX + halfL + 5f, frontY, 3.8f, eyeletFill)
        canvas.drawCircle(viewCenterX + halfL + 5f, frontY, 3.8f, eyeletStroke)

        // 3D Strike Eye (Positioned per silhouette)
        val eyeCenter = getEyeCenter(params.silhouette, viewCenterX, frontY, halfL, halfW)
        val eyeBase = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL; isAntiAlias = true }
        val eyePupil = Paint().apply { color = Color.rgb(15, 23, 42); style = Paint.Style.FILL; isAntiAlias = true }
        canvas.drawCircle(eyeCenter.x, eyeCenter.y, 3.2f, eyeBase)
        canvas.drawCircle(eyeCenter.x, eyeCenter.y, 3.2f, eyeletStroke)
        canvas.drawCircle(eyeCenter.x + 0.8f, eyeCenter.y, 1.6f, eyePupil)

        // Front Ring Representation (Requirement 15, 20)
        var frontRingRightX = viewCenterX - halfL - 5f
        if (params.hasFrontRing) {
            val ringRadius = if (params.isFrontRingHeavyDuty) 6.8f else 5.2f
            val ringStrokeW = if (params.isFrontRingHeavyDuty) 2.2f else 1.5f
            val ringCenterX = viewCenterX - halfL - 10f
            frontRingRightX = ringCenterX

            val ringPaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                style = Paint.Style.STROKE
                strokeWidth = ringStrokeW
                isAntiAlias = true
            }
            canvas.drawCircle(ringCenterX, frontY, ringRadius, eyeletFill)
            canvas.drawCircle(ringCenterX, frontY, ringRadius, ringPaint)

            // Front Ring Leader Callout (Collision-Free Upper Left)
            drawLeaderCalloutPdf(
                canvas = canvas,
                targetX = ringCenterX,
                targetY = frontY - ringRadius,
                deltaX = -18f,
                deltaY = -12f,
                shoulderLength = 55f,
                isLeft = true,
                text = "FRONT RING: ${params.frontRing.uppercase()}"
            )
        }

        // Back Ring Representation (Requirement 15, 21)
        if (params.hasBackRing) {
            val ringRadius = if (params.isBackRingHeavyDuty) 6.8f else 5.2f
            val ringStrokeW = if (params.isBackRingHeavyDuty) 2.2f else 1.5f
            val ringCenterX = viewCenterX + halfL + 10f

            val ringPaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                style = Paint.Style.STROKE
                strokeWidth = ringStrokeW
                isAntiAlias = true
            }
            canvas.drawCircle(ringCenterX, frontY, ringRadius, eyeletFill)
            canvas.drawCircle(ringCenterX, frontY, ringRadius, ringPaint)

            // Back Ring Leader Callout (Collision-Free Upper Right)
            drawLeaderCalloutPdf(
                canvas = canvas,
                targetX = ringCenterX,
                targetY = frontY - ringRadius,
                deltaX = 18f,
                deltaY = -12f,
                shoulderLength = 55f,
                isLeft = false,
                text = "BACK RING: ${params.backRing.uppercase()}"
            )
        }

        // Assist Hook & Assist Cord Representation (Requirement 18, 19)
        if (params.hasAssistHook) {
            val hookAttachX = viewCenterX - halfL - 5f
            val cordEndX = hookAttachX - 14f
            val cordEndY = frontY + 20f

            // Braided Assist Cord line
            val cordPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                strokeWidth = 2.0f
                isAntiAlias = true
            }
            canvas.drawLine(hookAttachX, frontY, cordEndX, cordEndY, cordPaint)

            // Whip-finished thread wrapping over hook shank in user's configured color
            if (params.hasAssistCord) {
                val threadWrapPaint = Paint().apply {
                    color = params.assistCordColorInt
                    strokeWidth = 3.2f
                    isAntiAlias = true
                }
                canvas.drawLine(hookAttachX - 5f, frontY + 7f, cordEndX + 2f, cordEndY - 3f, threadWrapPaint)
            }

            // Hook Eye & Forged Shank & Barb
            val hookPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                style = Paint.Style.STROKE
                strokeWidth = 1.8f
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = true
            }
            canvas.drawCircle(cordEndX, cordEndY, 2.5f, eyeletFill)
            canvas.drawCircle(cordEndX, cordEndY, 2.5f, eyeletStroke)

            val hookPath = Path().apply {
                moveTo(cordEndX, cordEndY)
                lineTo(cordEndX - 4f, cordEndY + 12f)
                cubicTo(
                    cordEndX - 6f, cordEndY + 22f,
                    cordEndX + 10f, cordEndY + 26f,
                    cordEndX + 14f, cordEndY + 16f
                )
                lineTo(cordEndX + 13f, cordEndY + 12f)
            }
            canvas.drawPath(hookPath, hookPaint)

            // Assist Hook Callout (Collision-Free Bottom-Left)
            drawLeaderCalloutPdf(
                canvas = canvas,
                targetX = cordEndX + 10f,
                targetY = cordEndY + 22f,
                deltaX = -20f,
                deltaY = 10f,
                shoulderLength = 65f,
                isLeft = true,
                text = "ASSIST HOOK: ${params.assistHook.uppercase()}"
            )

            // Assist Cord Callout (Collision-Free Mid-Left)
            if (params.hasAssistCord) {
                drawLeaderCalloutPdf(
                    canvas = canvas,
                    targetX = hookAttachX - 9f,
                    targetY = frontY + 12f,
                    deltaX = -22f,
                    deltaY = -4f,
                    shoulderLength = 55f,
                    isLeft = true,
                    text = "ASSIST CORD: ${params.assistCord.uppercase()}"
                )
            }
        }

        // =====================================================================
        // PARAMETRIC DIMENSIONS (Collision-Free Algorithm per Section 26 & 28)
        // =====================================================================
        if (showDimensions) {
            // Overall Length Dimension Line (Positioned above Front View)
            val lengthDimY = frontY - halfW - 14f
            drawParametricDimensionPdf(
                canvas = canvas,
                x1 = viewCenterX - halfL,
                y1 = frontY,
                x2 = viewCenterX + halfL,
                y2 = frontY,
                dimOffset = -halfW - 14f,
                isHorizontal = true,
                label = "${params.lengthMm.toInt()} mm"
            )

            // Maximum Body Width Dimension Line (Positioned right of Front View)
            val widthDimX = viewCenterX + halfL + 18f
            drawParametricDimensionPdf(
                canvas = canvas,
                x1 = viewCenterX,
                y1 = frontY - halfW,
                x2 = viewCenterX,
                y2 = frontY + halfW,
                dimOffset = halfL + 18f,
                isHorizontal = false,
                label = "${params.widthMm.toInt()} mm"
            )
        }
        } // End View 1

        // =====================================================================
        // VIEW 2: TOP PLAN VIEW (Altered longitudinally per Jig silhouette)
        // =====================================================================
        if (perspective == EngineeringPerspective.ORTHOGRAPHIC || perspective == EngineeringPerspective.TOP_PLAN) {
            val topTitle = if (perspective == EngineeringPerspective.TOP_PLAN) "TOP PLAN VIEW (SCALE 1.3:1)" else "TOP PLAN VIEW (SCALE 1:1)"
            canvas.drawText(topTitle, boxX + 16f, topY - halfW * 0.5f - 16f, viewLabelPaint)

            // Plan Centerline
            canvas.drawLine(viewCenterX - halfL - 25f, topY, viewCenterX + halfL + 25f, topY, centerLinePaint)

            val topThickness = halfW * 0.45f
            val topPath = buildTopSilhouettePath(params.silhouette, viewCenterX, topY, halfL, topThickness)
            val topFill = Paint().apply { color = Color.rgb(241, 245, 249); style = Paint.Style.FILL; isAntiAlias = true }
            canvas.drawPath(topPath, topFill)
            canvas.drawPath(topPath, outlinePaint)

            // Spine / Ridge line running longitudinally
            val spinePaint = Paint().apply { color = Color.rgb(2, 132, 199); strokeWidth = 1.0f; isAntiAlias = true }
            canvas.drawLine(viewCenterX - halfL + 6f, topY, viewCenterX + halfL - 6f, topY, spinePaint)

            // Plan Eyelets
            canvas.drawCircle(viewCenterX - halfL - 5f, topY, 2.8f, eyeletStroke)
            canvas.drawCircle(viewCenterX + halfL + 5f, topY, 2.8f, eyeletStroke)

            // Thickness Dimension
            if (showDimensions) {
                drawParametricDimensionPdf(
                    canvas = canvas,
                    x1 = viewCenterX,
                    y1 = topY - topThickness,
                    x2 = viewCenterX,
                    y2 = topY + topThickness,
                    dimOffset = halfL + 18f,
                    isHorizontal = false,
                    label = "${(params.widthMm * 0.45f).toInt()} mm"
                )
            }
        }

        // =====================================================================
        // VIEW 3: END / SECTION ELEVATION (Right Column, Upper)
        // =====================================================================
        if (perspective == EngineeringPerspective.ORTHOGRAPHIC) {
            canvas.drawText("END PROFILE", endCenterX - 28f, endCenterY - halfW - 20f, viewLabelPaint)
            canvas.drawLine(endCenterX - 25f, endCenterY, endCenterX + 25f, endCenterY, centerLinePaint)
            canvas.drawLine(endCenterX, endCenterY - halfW - 10f, endCenterX, endCenterY + halfW + 10f, centerLinePaint)

            val endPath = buildEndSilhouettePath(params.silhouette, endCenterX, endCenterY, halfW * 0.6f, halfW)
            val endFill = Paint().apply { color = Color.rgb(241, 245, 249); style = Paint.Style.FILL; isAntiAlias = true }
            canvas.drawPath(endPath, endFill)
            canvas.drawPath(endPath, outlinePaint)
        }

        // =====================================================================
        // VIEW 4: AXONOMETRIC ISOMETRIC REFERENCE
        // =====================================================================
        if (perspective == EngineeringPerspective.ORTHOGRAPHIC || perspective == EngineeringPerspective.ISOMETRIC) {
            val isoTitle = if (perspective == EngineeringPerspective.ISOMETRIC) "3D AXONOMETRIC ISOMETRIC VIEW" else "ISOMETRIC REFERENCE"
            val titleY = if (perspective == EngineeringPerspective.ISOMETRIC) isoCenterY - 45f else isoCenterY - 32f
            canvas.drawText(isoTitle, isoCenterX - 45f, titleY, viewLabelPaint)
            val isoL = if (perspective == EngineeringPerspective.ISOMETRIC) 75f else 38f
            val isoW = if (perspective == EngineeringPerspective.ISOMETRIC) 20f else 10f
            drawIsometricJigPdf(canvas, params.silhouette, isoCenterX, isoCenterY, isoL, isoW, fillPaint, outlinePaint)
        }

        // =====================================================================
        // VIEW 5: DYNAMIC CONFIGURATION SUMMARY & LEGEND (Section 24 & 25)
        // Strictly reflects the active user configuration with zero static noise.
        // =====================================================================
        val legendTop = boxY + boxHeight - 82f
        if (perspective == EngineeringPerspective.ORTHOGRAPHIC) {
            drawDynamicLegendPdf(canvas, boxX + 12f, legendTop, boxWidth - 190f, 72f, params)
        }

        // =====================================================================
        // VIEW 6: OFFICIAL TITLE BLOCK (Section 38 & 39)
        // Fully dynamic, showing actual product, model, target weight, date.
        // =====================================================================
        val tbX = boxX + boxWidth - 165f
        val tbY = legendTop
        drawDynamicTitleBlockPdf(canvas, tbX, tbY, 155f, 72f, params)
    }

    /**
     * Builds the authentic parametric 2D front silhouette path.
     */
    fun buildFrontSilhouettePath(
        silhouette: JigSilhouetteType,
        cx: Float,
        cy: Float,
        halfL: Float,
        halfW: Float
    ): Path {
        return Path().apply {
            when (silhouette) {
                // =============================================================
                // 1. BASIC / GEOMETRIC
                // =============================================================
                JigSilhouetteType.ROUND, JigSilhouetteType.BALL_HEAD -> {
                    // True circular spherical ballast profile
                    addCircle(cx, cy, min(halfL, halfW), Path.Direction.CW)
                }

                JigSilhouetteType.CIRCULAR, JigSilhouetteType.ROUND_HEAD -> {
                    // Flattened coin / radial profile
                    addOval(RectF(cx - halfL, cy - halfW, cx + halfL, cy + halfW), Path.Direction.CW)
                }

                JigSilhouetteType.OVAL -> {
                    // Smooth elongated ellipse
                    addOval(RectF(cx - halfL, cy - halfW, cx + halfL, cy + halfW), Path.Direction.CW)
                }

                JigSilhouetteType.DISC -> {
                    // Flat lens / disc profile with rounded convex ends
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.5f, cy - halfW, cx + halfL * 0.5f, cy - halfW, cx + halfL, cy)
                    cubicTo(cx + halfL * 0.5f, cy + halfW, cx - halfL * 0.5f, cy + halfW, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.FLAT_DISC -> {
                    // Planar disc with crisp chamfered corners
                    moveTo(cx - halfL + 6f, cy - halfW)
                    lineTo(cx + halfL - 6f, cy - halfW)
                    lineTo(cx + halfL, cy - halfW + 6f)
                    lineTo(cx + halfL, cy + halfW - 6f)
                    lineTo(cx + halfL - 6f, cy + halfW)
                    lineTo(cx - halfL + 6f, cy + halfW)
                    lineTo(cx - halfL, cy + halfW - 6f)
                    lineTo(cx - halfL, cy - halfW + 6f)
                    close()
                }

                JigSilhouetteType.TEARDROP, JigSilhouetteType.SHORE_CAST_TEARDROP -> {
                    // Narrow head expanding to bulbous posterior
                    moveTo(cx - halfL, cy)
                    cubicTo(
                        cx - halfL * 0.65f, cy - halfW * 0.95f,
                        cx - halfL * 0.25f, cy - halfW * 1.00f,
                        cx, cy - halfW * 0.85f
                    )
                    cubicTo(
                        cx + halfL * 0.40f, cy - halfW * 0.65f,
                        cx + halfL * 0.75f, cy - halfW * 0.35f,
                        cx + halfL, cy
                    )
                    cubicTo(
                        cx + halfL * 0.75f, cy + halfW * 0.35f,
                        cx + halfL * 0.40f, cy + halfW * 0.65f,
                        cx, cy + halfW * 0.85f
                    )
                    cubicTo(
                        cx - halfL * 0.25f, cy + halfW * 1.00f,
                        cx - halfL * 0.65f, cy + halfW * 0.95f,
                        cx - halfL, cy
                    )
                    close()
                }

                JigSilhouetteType.PEAR -> {
                    // Piriform pear shape - forward pinch expanding to bottom-heavy belly
                    moveTo(cx - halfL, cy)
                    cubicTo(
                        cx - halfL * 0.5f, cy - halfW * 0.4f,
                        cx + halfL * 0.1f, cy - halfW * 0.95f,
                        cx + halfL * 0.6f, cy - halfW
                    )
                    cubicTo(
                        cx + halfL * 0.9f, cy - halfW * 0.7f,
                        cx + halfL, cy - halfW * 0.3f,
                        cx + halfL, cy
                    )
                    cubicTo(
                        cx + halfL, cy + halfW * 0.3f,
                        cx + halfL * 0.9f, cy + halfW * 0.7f,
                        cx + halfL * 0.6f, cy + halfW
                    )
                    cubicTo(
                        cx + halfL * 0.1f, cy + halfW * 0.95f,
                        cx - halfL * 0.5f, cy + halfW * 0.4f,
                        cx - halfL, cy
                    )
                    close()
                }

                JigSilhouetteType.EGG -> {
                    // Ovoid profile peaking towards rear third
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.6f, cy - halfW * 0.7f, cx + halfL * 0.2f, cy - halfW, cx + halfL * 0.5f, cy - halfW)
                    cubicTo(cx + halfL * 0.85f, cy - halfW * 0.8f, cx + halfL, cy - halfW * 0.4f, cx + halfL, cy)
                    cubicTo(cx + halfL, cy + halfW * 0.4f, cx + halfL * 0.85f, cy + halfW * 0.8f, cx + halfL * 0.5f, cy + halfW)
                    cubicTo(cx + halfL * 0.2f, cy + halfW, cx - halfL * 0.6f, cy + halfW * 0.7f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.BULLET -> {
                    // Ogive parabolic nose with flat base
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.6f, cy - halfW * 0.8f, cx - halfL * 0.1f, cy - halfW, cx + halfL * 0.6f, cy - halfW)
                    lineTo(cx + halfL, cy - halfW)
                    lineTo(cx + halfL, cy + halfW)
                    lineTo(cx + halfL * 0.6f, cy + halfW)
                    cubicTo(cx - halfL * 0.1f, cy + halfW, cx - halfL * 0.6f, cy + halfW * 0.8f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.CONE -> {
                    // Sharp conical nose expanding linearly to planar base
                    moveTo(cx - halfL, cy)
                    lineTo(cx + halfL, cy - halfW)
                    lineTo(cx + halfL, cy + halfW)
                    close()
                }

                JigSilhouetteType.CYLINDER -> {
                    // Parallel cylinder with rounded cap ends
                    addRoundRect(
                        RectF(cx - halfL, cy - halfW, cx + halfL, cy + halfW),
                        8f, 8f, Path.Direction.CW
                    )
                }

                JigSilhouetteType.TAPERED_CYLINDER -> {
                    // Trapezoidal body with filleted corners
                    moveTo(cx - halfL, cy - halfW * 0.45f)
                    lineTo(cx + halfL, cy - halfW)
                    lineTo(cx + halfL, cy + halfW)
                    lineTo(cx - halfL, cy + halfW * 0.45f)
                    close()
                }

                // =============================================================
                // 2. HEAD / JIG STYLES
                // =============================================================
                JigSilhouetteType.FOOTBALL_HEAD -> {
                    // Wide lateral football cross-axis head with rear shank collar
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.8f, cy - halfW * 1.1f, cx - halfL * 0.2f, cy - halfW * 1.1f, cx, cy - halfW * 0.8f)
                    lineTo(cx + halfL * 0.7f, cy - halfW * 0.35f)
                    lineTo(cx + halfL, cy - halfW * 0.20f)
                    lineTo(cx + halfL, cy + halfW * 0.20f)
                    lineTo(cx + halfL * 0.7f, cy + halfW * 0.35f)
                    lineTo(cx, cy + halfW * 0.8f)
                    cubicTo(cx - halfL * 0.2f, cy + halfW * 1.1f, cx - halfL * 0.8f, cy + halfW * 1.1f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.ARKIE_HEAD -> {
                    // Modified arkie wedge with flat planing bottom
                    moveTo(cx - halfL, cy - halfW * 0.4f)
                    cubicTo(cx - halfL * 0.5f, cy - halfW * 0.95f, cx, cy - halfW * 0.95f, cx + halfL * 0.4f, cy - halfW * 0.6f)
                    lineTo(cx + halfL, cy - halfW * 0.25f)
                    lineTo(cx + halfL, cy + halfW * 0.25f)
                    lineTo(cx + halfL * 0.2f, cy + halfW * 0.8f)
                    lineTo(cx - halfL * 0.6f, cy + halfW * 0.8f)
                    lineTo(cx - halfL, cy + halfW * 0.3f)
                    close()
                }

                JigSilhouetteType.FINESSE_HEAD -> {
                    // Low profile compact teardrop with slender collar
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.7f, cy - halfW * 0.9f, cx - halfL * 0.1f, cy - halfW * 0.9f, cx + halfL * 0.2f, cy - halfW * 0.5f)
                    lineTo(cx + halfL, cy - halfW * 0.25f)
                    lineTo(cx + halfL, cy + halfW * 0.25f)
                    lineTo(cx + halfL * 0.2f, cy + halfW * 0.5f)
                    cubicTo(cx - halfL * 0.1f, cy + halfW * 0.9f, cx - halfL * 0.7f, cy + halfW * 0.9f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.MUSHROOM_HEAD, JigSilhouetteType.NED_HEAD -> {
                    // Hemispherical mushroom dome with flat vertical trailing face
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.9f, cy - halfW * 0.95f, cx - halfL * 0.1f, cy - halfW, cx + halfL * 0.3f, cy - halfW)
                    lineTo(cx + halfL * 0.3f, cy - halfW * 0.3f)
                    lineTo(cx + halfL, cy - halfW * 0.2f)
                    lineTo(cx + halfL, cy + halfW * 0.2f)
                    lineTo(cx + halfL * 0.3f, cy + halfW * 0.3f)
                    lineTo(cx + halfL * 0.3f, cy + halfW)
                    cubicTo(cx - halfL * 0.1f, cy + halfW, cx - halfL * 0.9f, cy + halfW * 0.95f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.DART_HEAD, JigSilhouetteType.DART_BLADE -> {
                    // Arrowhead wedge pointed nose with angular swept flukes
                    moveTo(cx - halfL, cy)
                    lineTo(cx + halfL * 0.1f, cy - halfW)
                    lineTo(cx + halfL * 0.4f, cy - halfW * 0.5f)
                    lineTo(cx + halfL, cy - halfW * 0.2f)
                    lineTo(cx + halfL, cy + halfW * 0.2f)
                    lineTo(cx + halfL * 0.4f, cy + halfW * 0.5f)
                    lineTo(cx + halfL * 0.1f, cy + halfW)
                    close()
                }

                JigSilhouetteType.STAND_UP_HEAD -> {
                    // Flat-bottomed triangular stand-up planing jig
                    moveTo(cx - halfL, cy - halfW * 0.2f)
                    lineTo(cx + halfL * 0.2f, cy - halfW * 0.9f)
                    lineTo(cx + halfL, cy - halfW * 0.25f)
                    lineTo(cx + halfL, cy + halfW * 0.35f)
                    lineTo(cx - halfL * 0.3f, cy + halfW)
                    lineTo(cx - halfL, cy + halfW)
                    close()
                }

                JigSilhouetteType.SWIMBAIT_HEAD -> {
                    // Realistic hydrodynamic minnow head with gill flare
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.6f, cy - halfW * 0.7f, cx - halfL * 0.1f, cy - halfW * 0.9f, cx + halfL * 0.3f, cy - halfW * 0.7f)
                    lineTo(cx + halfL * 0.35f, cy - halfW * 0.4f)
                    lineTo(cx + halfL, cy - halfW * 0.2f)
                    lineTo(cx + halfL, cy + halfW * 0.2f)
                    lineTo(cx + halfL * 0.35f, cy + halfW * 0.4f)
                    lineTo(cx + halfL * 0.3f, cy + halfW * 0.7f)
                    cubicTo(cx - halfL * 0.1f, cy + halfW * 0.9f, cx - halfL * 0.6f, cy + halfW * 0.7f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.SHAKY_HEAD -> {
                    // Round/football hybrid with screwlock collar
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.7f, cy - halfW * 0.95f, cx, cy - halfW * 0.95f, cx + halfL * 0.3f, cy - halfW * 0.5f)
                    lineTo(cx + halfL, cy - halfW * 0.2f)
                    lineTo(cx + halfL, cy + halfW * 0.2f)
                    lineTo(cx + halfL * 0.3f, cy + halfW * 0.5f)
                    cubicTo(cx, cy + halfW * 0.95f, cx - halfL * 0.7f, cy + halfW * 0.95f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.TUBE_HEAD -> {
                    // Cylindrical insert head tapered at nose
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.7f, cy - halfW * 0.8f, cx - halfL * 0.3f, cy - halfW, cx, cy - halfW)
                    lineTo(cx + halfL, cy - halfW * 0.85f)
                    lineTo(cx + halfL, cy + halfW * 0.85f)
                    lineTo(cx, cy + halfW)
                    cubicTo(cx - halfL * 0.3f, cy + halfW, cx - halfL * 0.7f, cy + halfW * 0.8f, cx - halfL, cy)
                    close()
                }

                // =============================================================
                // 3. FISHING-ORIENTED BODY SHAPES
                // =============================================================
                JigSilhouetteType.LONG_SHAD -> {
                    // Slender baitfish profile with deep belly chord and slender tail
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.6f, cy - halfW * 0.8f, cx - halfL * 0.1f, cy - halfW * 0.95f, cx + halfL * 0.3f, cy - halfW * 0.6f)
                    cubicTo(cx + halfL * 0.7f, cy - halfW * 0.3f, cx + halfL * 0.9f, cy - halfW * 0.1f, cx + halfL, cy)
                    cubicTo(cx + halfL * 0.9f, cy + halfW * 0.1f, cx + halfL * 0.6f, cy + halfW * 0.4f, cx + halfL * 0.1f, cy + halfW * 0.95f)
                    cubicTo(cx - halfL * 0.3f, cy + halfW * 1.0f, cx - halfL * 0.7f, cy + halfW * 0.6f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.SHORT_SHAD -> {
                    // Compact deep-bodied shad profile
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.5f, cy - halfW * 0.9f, cx - halfL * 0.1f, cy - halfW, cx + halfL * 0.2f, cy - halfW * 0.7f)
                    cubicTo(cx + halfL * 0.6f, cy - halfW * 0.4f, cx + halfL * 0.85f, cy - halfW * 0.15f, cx + halfL, cy)
                    cubicTo(cx + halfL * 0.85f, cy + halfW * 0.15f, cx + halfL * 0.5f, cy + halfW * 0.5f, cx, cy + halfW)
                    cubicTo(cx - halfL * 0.4f, cy + halfW * 1.0f, cx - halfL * 0.75f, cy + halfW * 0.7f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.MINNOW, JigSilhouetteType.SLIM, JigSilhouetteType.SARDINE -> {
                    // Classic streamlined minnow baitfish silhouette
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.6f, cy - halfW * 0.7f, cx - halfL * 0.1f, cy - halfW * 0.85f, cx + halfL * 0.3f, cy - halfW * 0.7f)
                    cubicTo(cx + halfL * 0.7f, cy - halfW * 0.4f, cx + halfL * 0.9f, cy - halfW * 0.15f, cx + halfL, cy)
                    cubicTo(cx + halfL * 0.9f, cy + halfW * 0.15f, cx + halfL * 0.7f, cy + halfW * 0.4f, cx + halfL * 0.3f, cy + halfW * 0.7f)
                    cubicTo(cx - halfL * 0.1f, cy + halfW * 0.85f, cx - halfL * 0.6f, cy + halfW * 0.7f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.BAITFISH, JigSilhouetteType.HERRING -> {
                    // Herring with broad dorsal shoulder
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.5f, cy - halfW * 0.95f, cx, cy - halfW * 0.9f, cx + halfL * 0.4f, cy - halfW * 0.6f)
                    cubicTo(cx + halfL * 0.75f, cy - halfW * 0.3f, cx + halfL * 0.9f, cy - halfW * 0.1f, cx + halfL, cy)
                    cubicTo(cx + halfL * 0.9f, cy + halfW * 0.1f, cx + halfL * 0.6f, cy + halfW * 0.5f, cx + halfL * 0.1f, cy + halfW * 0.9f)
                    cubicTo(cx - halfL * 0.3f, cy + halfW * 0.95f, cx - halfL * 0.7f, cy + halfW * 0.6f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.NEEDLE, JigSilhouetteType.VERTICAL_NEEDLE_NOSE -> {
                    // Slender needle forward, rear-weighted bulbous flair
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.40f, cy - halfW * 0.35f, cx + halfL * 0.10f, cy - halfW * 0.50f, cx + halfL * 0.55f, cy - halfW * 0.95f)
                    cubicTo(cx + halfL * 0.75f, cy - halfW * 1.00f, cx + halfL * 0.92f, cy - halfW * 0.50f, cx + halfL, cy)
                    cubicTo(cx + halfL * 0.92f, cy + halfW * 0.50f, cx + halfL * 0.75f, cy + halfW * 1.00f, cx + halfL * 0.55f, cy + halfW * 0.95f)
                    cubicTo(cx + halfL * 0.10f, cy + halfW * 0.50f, cx - halfL * 0.40f, cy + halfW * 0.35f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.WIDE_BODY, JigSilhouetteType.DEEP_BODY -> {
                    // Deep slab-sided body with heavy displacement
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.6f, cy - halfW * 1.1f, cx, cy - halfW * 1.1f, cx + halfL * 0.5f, cy - halfW * 0.75f)
                    cubicTo(cx + halfL * 0.8f, cy - halfW * 0.4f, cx + halfL * 0.95f, cy - halfW * 0.15f, cx + halfL, cy)
                    cubicTo(cx + halfL * 0.95f, cy + halfW * 0.15f, cx + halfL * 0.8f, cy + halfW * 0.4f, cx + halfL * 0.5f, cy + halfW * 0.75f)
                    cubicTo(cx, cy + halfW * 1.1f, cx - halfL * 0.6f, cy + halfW * 1.1f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.PADDLE, JigSilhouetteType.HAMMER -> {
                    // Body expanding rearward to wide paddle/hammer tail
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.5f, cy - halfW * 0.4f, cx, cy - halfW * 0.5f, cx + halfL * 0.4f, cy - halfW * 0.6f)
                    lineTo(cx + halfL * 0.7f, cy - halfW)
                    lineTo(cx + halfL, cy - halfW * 0.9f)
                    lineTo(cx + halfL, cy + halfW * 0.9f)
                    lineTo(cx + halfL * 0.7f, cy + halfW)
                    lineTo(cx + halfL * 0.4f, cy + halfW * 0.6f)
                    cubicTo(cx, cy + halfW * 0.5f, cx - halfL * 0.5f, cy + halfW * 0.4f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.BLADE, JigSilhouetteType.TEAR_BLADE -> {
                    // Precision stamped blade with high-speed hydrofoil curvature
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.5f, cy - halfW * 0.95f, cx + halfL * 0.2f, cy - halfW * 0.95f, cx + halfL * 0.7f, cy - halfW * 0.4f)
                    lineTo(cx + halfL, cy)
                    lineTo(cx + halfL * 0.7f, cy + halfW * 0.4f)
                    cubicTo(cx + halfL * 0.2f, cy + halfW * 0.95f, cx - halfL * 0.5f, cy + halfW * 0.95f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.SPOON -> {
                    // Deep concave cupped spoon profile
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.6f, cy - halfW * 0.7f, cx - halfL * 0.1f, cy - halfW * 1.1f, cx + halfL * 0.4f, cy - halfW * 0.9f)
                    cubicTo(cx + halfL * 0.8f, cy - halfW * 0.6f, cx + halfL, cy - halfW * 0.3f, cx + halfL, cy)
                    cubicTo(cx + halfL, cy + halfW * 0.3f, cx + halfL * 0.8f, cy + halfW * 0.6f, cx + halfL * 0.4f, cy + halfW * 0.9f)
                    cubicTo(cx - halfL * 0.1f, cy + halfW * 1.1f, cx - halfL * 0.6f, cy + halfW * 0.7f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.LEAF, JigSilhouetteType.SLOW_PITCH_DIAMOND -> {
                    // Willow leaf / diamond profile peaking at 50% length
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.60f, cy - halfW * 0.75f, cx - halfL * 0.15f, cy - halfW * 1.00f, cx, cy - halfW)
                    cubicTo(cx + halfL * 0.15f, cy - halfW * 1.00f, cx + halfL * 0.60f, cy - halfW * 0.75f, cx + halfL, cy)
                    cubicTo(cx + halfL * 0.60f, cy + halfW * 0.75f, cx + halfL * 0.15f, cy + halfW * 1.00f, cx, cy + halfW)
                    cubicTo(cx - halfL * 0.15f, cy + halfW * 1.00f, cx - halfL * 0.60f, cy + halfW * 0.75f, cx - halfL, cy)
                    close()
                }

                // =============================================================
                // 4. SPECIAL / DISTINCTIVE SILHOUETTES
                // =============================================================
                JigSilhouetteType.DIAMOND -> {
                    // Rhomboid geometric diamond
                    moveTo(cx - halfL, cy)
                    lineTo(cx, cy - halfW)
                    lineTo(cx + halfL, cy)
                    lineTo(cx, cy + halfW)
                    close()
                }

                JigSilhouetteType.HEXAGON -> {
                    // Elongated hexagon prism
                    moveTo(cx - halfL, cy)
                    lineTo(cx - halfL * 0.5f, cy - halfW)
                    lineTo(cx + halfL * 0.5f, cy - halfW)
                    lineTo(cx + halfL, cy)
                    lineTo(cx + halfL * 0.5f, cy + halfW)
                    lineTo(cx - halfL * 0.5f, cy + halfW)
                    close()
                }

                JigSilhouetteType.TRIANGLE -> {
                    // Isosceles wedge triangle
                    moveTo(cx - halfL, cy)
                    lineTo(cx + halfL, cy - halfW)
                    lineTo(cx + halfL, cy + halfW)
                    close()
                }

                JigSilhouetteType.CRESCENT -> {
                    // Curved crescent moon arc
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.4f, cy - halfW * 1.1f, cx + halfL * 0.4f, cy - halfW * 1.1f, cx + halfL, cy)
                    cubicTo(cx + halfL * 0.3f, cy - halfW * 0.3f, cx - halfL * 0.3f, cy - halfW * 0.3f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.SPLIT_BODY, JigSilhouetteType.TWIN_PROFILE -> {
                    // Dual-chine split keel profile
                    moveTo(cx - halfL, cy)
                    cubicTo(cx - halfL * 0.5f, cy - halfW * 0.9f, cx - halfL * 0.1f, cy - halfW * 0.9f, cx, cy - halfW * 0.5f)
                    cubicTo(cx + halfL * 0.1f, cy - halfW * 0.9f, cx + halfL * 0.5f, cy - halfW * 0.9f, cx + halfL, cy)
                    cubicTo(cx + halfL * 0.5f, cy + halfW * 0.9f, cx + halfL * 0.1f, cy + halfW * 0.9f, cx, cy + halfW * 0.5f)
                    cubicTo(cx - halfL * 0.1f, cy + halfW * 0.9f, cx - halfL * 0.5f, cy + halfW * 0.9f, cx - halfL, cy)
                    close()
                }

                JigSilhouetteType.ASYMMETRIC, JigSilhouetteType.ASYMMETRIC_HYDRO_KEEL -> {
                    // Orange-Black: High dorsal shoulder at 35% chord, knife-edge entry, hydro-keel belly
                    moveTo(cx - halfL, cy)
                    cubicTo(
                        cx - halfL * 0.55f, cy - halfW * 0.90f,
                        cx - halfL * 0.10f, cy - halfW * 1.00f,
                        cx + halfL * 0.40f, cy - halfW * 0.70f
                    )
                    cubicTo(
                        cx + halfL * 0.70f, cy - halfW * 0.45f,
                        cx + halfL * 0.90f, cy - halfW * 0.20f,
                        cx + halfL, cy
                    )
                    cubicTo(
                        cx + halfL * 0.85f, cy + halfW * 0.35f,
                        cx + halfL * 0.30f, cy + halfW * 0.85f,
                        cx - halfL * 0.20f, cy + halfW * 0.95f
                    )
                    cubicTo(
                        cx - halfL * 0.60f, cy + halfW * 0.75f,
                        cx - halfL * 0.85f, cy + halfW * 0.35f,
                        cx - halfL, cy
                    )
                    close()
                }

                // =============================================================
                // 5. 7HOOKS SIGNATURE SERIES
                // =============================================================
                JigSilhouetteType.PELAGIC_S_CURVE -> {
                    // Candy Blue-Orange: Hydrodynamic S-curve inflection
                    moveTo(cx - halfL, cy)
                    cubicTo(
                        cx - halfL * 0.50f, cy - halfW * 1.00f,
                        cx - halfL * 0.10f, cy - halfW * 0.80f,
                        cx + halfL * 0.20f, cy - halfW * 0.50f
                    )
                    cubicTo(
                        cx + halfL * 0.60f, cy - halfW * 0.70f,
                        cx + halfL * 0.85f, cy - halfW * 0.30f,
                        cx + halfL, cy
                    )
                    cubicTo(
                        cx + halfL * 0.70f, cy + halfW * 0.60f,
                        cx + halfL * 0.20f, cy + halfW * 0.95f,
                        cx - halfL * 0.20f, cy + halfW * 0.80f
                    )
                    cubicTo(
                        cx - halfL * 0.60f, cy + halfW * 0.50f,
                        cx - halfL * 0.85f, cy + halfW * 0.25f,
                        cx - halfL, cy
                    )
                    close()
                }

                JigSilhouetteType.STEPPED_HYDROFOIL -> {
                    // Candy Yellow-Black: Stepped chine edge
                    moveTo(cx - halfL, cy)
                    lineTo(cx - halfL * 0.50f, cy - halfW * 0.80f)
                    lineTo(cx - halfL * 0.48f, cy - halfW * 0.95f)
                    lineTo(cx + halfL * 0.20f, cy - halfW * 0.95f)
                    lineTo(cx + halfL * 0.22f, cy - halfW * 0.80f)
                    lineTo(cx + halfL, cy)
                    cubicTo(
                        cx + halfL * 0.60f, cy + halfW * 0.75f,
                        cx - halfL * 0.10f, cy + halfW * 0.90f,
                        cx - halfL * 0.60f, cy + halfW * 0.65f
                    )
                    close()
                }

                JigSilhouetteType.CRYSTAL_FACETED -> {
                    // Crystal faceted: Multi-point polygon
                    moveTo(cx - halfL, cy)
                    lineTo(cx - halfL * 0.60f, cy - halfW * 0.75f)
                    lineTo(cx - halfL * 0.20f, cy - halfW)
                    lineTo(cx + halfL * 0.40f, cy - halfW * 0.85f)
                    lineTo(cx + halfL * 0.75f, cy - halfW * 0.45f)
                    lineTo(cx + halfL, cy)
                    lineTo(cx + halfL * 0.75f, cy + halfW * 0.45f)
                    lineTo(cx + halfL * 0.40f, cy + halfW * 0.85f)
                    lineTo(cx - halfL * 0.20f, cy + halfW)
                    lineTo(cx - halfL * 0.60f, cy + halfW * 0.75f)
                    close()
                }
            }
        }
    }

    /**
     * Draws authentic internal break-lines, facets, and keel ridges.
     */
    private fun drawSilhouetteFeaturesPdf(
        canvas: Canvas,
        silhouette: JigSilhouetteType,
        cx: Float,
        cy: Float,
        halfL: Float,
        halfW: Float,
        paint: Paint
    ) {
        when (silhouette) {
            JigSilhouetteType.ASYMMETRIC_HYDRO_KEEL, JigSilhouetteType.ASYMMETRIC -> {
                // Keel ridge line separating flat belly and angled dorsal face
                val keelPath = Path().apply {
                    moveTo(cx - halfL, cy)
                    cubicTo(
                        cx - halfL * 0.40f, cy + halfW * 0.15f,
                        cx + halfL * 0.20f, cy + halfW * 0.25f,
                        cx + halfL, cy
                    )
                }
                canvas.drawPath(keelPath, paint)
            }

            JigSilhouetteType.SLOW_PITCH_DIAMOND, JigSilhouetteType.DIAMOND, JigSilhouetteType.LEAF -> {
                // Diamond center facet crease and lateral cross lines
                canvas.drawLine(cx - halfL + 8f, cy, cx + halfL - 8f, cy, paint)
                canvas.drawLine(cx, cy - halfW, cx, cy + halfW, paint)
                canvas.drawLine(cx - halfL * 0.4f, cy, cx, cy - halfW, paint)
                canvas.drawLine(cx - halfL * 0.4f, cy, cx, cy + halfW, paint)
                canvas.drawLine(cx + halfL * 0.4f, cy, cx, cy - halfW, paint)
                canvas.drawLine(cx + halfL * 0.4f, cy, cx, cy + halfW, paint)
            }

            JigSilhouetteType.VERTICAL_NEEDLE_NOSE, JigSilhouetteType.NEEDLE, JigSilhouetteType.SLIM -> {
                // Hydro-stabilizer flute along needle body
                canvas.drawLine(cx - halfL * 0.70f, cy, cx + halfL * 0.50f, cy, paint)
            }

            JigSilhouetteType.PELAGIC_S_CURVE -> {
                // S-curve lateral line
                val sLine = Path().apply {
                    moveTo(cx - halfL * 0.70f, cy - halfW * 0.20f)
                    cubicTo(cx - halfL * 0.10f, cy + halfW * 0.15f, cx + halfL * 0.40f, cy - halfW * 0.15f, cx + halfL * 0.80f, cy)
                }
                canvas.drawPath(sLine, paint)
            }

            JigSilhouetteType.CRYSTAL_FACETED, JigSilhouetteType.HEXAGON -> {
                // Prismatic internal break lines
                canvas.drawLine(cx - halfL, cy, cx + halfL, cy, paint)
                canvas.drawLine(cx - halfL * 0.20f, cy - halfW, cx - halfL * 0.20f, cy + halfW, paint)
                canvas.drawLine(cx + halfL * 0.40f, cy - halfW * 0.85f, cx + halfL * 0.40f, cy + halfW * 0.85f, paint)
                canvas.drawLine(cx - halfL * 0.60f, cy - halfW * 0.75f, cx - halfL * 0.20f, cy, paint)
                canvas.drawLine(cx - halfL * 0.60f, cy + halfW * 0.75f, cx - halfL * 0.20f, cy, paint)
            }

            else -> {
                canvas.drawLine(cx - halfL * 0.80f, cy, cx + halfL * 0.80f, cy, paint)
            }
        }
    }

    /**
     * Resolves the coordinate of the 3D Strike Eye based on the silhouette.
     */
    private fun getEyeCenter(
        silhouette: JigSilhouetteType,
        cx: Float,
        cy: Float,
        halfL: Float,
        halfW: Float
    ): Point2D {
        return when (silhouette) {
            JigSilhouetteType.ROUND, JigSilhouetteType.BALL_HEAD, JigSilhouetteType.CIRCULAR ->
                Point2D(cx - halfL * 0.35f, cy - halfW * 0.25f)
            JigSilhouetteType.ASYMMETRIC_HYDRO_KEEL, JigSilhouetteType.ASYMMETRIC ->
                Point2D(cx - halfL + 18f, cy - halfW * 0.35f)
            JigSilhouetteType.SLOW_PITCH_DIAMOND, JigSilhouetteType.DIAMOND, JigSilhouetteType.LEAF ->
                Point2D(cx - halfL + 16f, cy)
            JigSilhouetteType.VERTICAL_NEEDLE_NOSE, JigSilhouetteType.NEEDLE ->
                Point2D(cx - halfL + 12f, cy - halfW * 0.20f)
            JigSilhouetteType.PELAGIC_S_CURVE ->
                Point2D(cx - halfL + 17f, cy - halfW * 0.30f)
            JigSilhouetteType.SHORE_CAST_TEARDROP, JigSilhouetteType.TEARDROP, JigSilhouetteType.PEAR ->
                Point2D(cx - halfL + 19f, cy - halfW * 0.25f)
            JigSilhouetteType.STEPPED_HYDROFOIL ->
                Point2D(cx - halfL + 16f, cy - halfW * 0.30f)
            JigSilhouetteType.CRYSTAL_FACETED ->
                Point2D(cx - halfL + 18f, cy - halfW * 0.30f)
            else ->
                Point2D(cx - halfL + 16f, cy - halfW * 0.20f)
        }
    }

    /**
     * Builds Top / Plan View Path.
     */
    private fun buildTopSilhouettePath(
        silhouette: JigSilhouetteType,
        cx: Float,
        cy: Float,
        halfL: Float,
        topThickness: Float
    ): Path {
        return Path().apply {
            moveTo(cx - halfL, cy)
            cubicTo(
                cx - halfL * 0.50f, cy - topThickness,
                cx + halfL * 0.20f, cy - topThickness * 1.10f,
                cx + halfL * 0.70f, cy - topThickness * 0.40f
            )
            lineTo(cx + halfL, cy)
            cubicTo(
                cx + halfL * 0.70f, cy + topThickness * 0.40f,
                cx + halfL * 0.20f, cy + topThickness * 1.10f,
                cx - halfL * 0.50f, cy + topThickness
            )
            close()
        }
    }

    /**
     * Builds End / Cross-Section View Path.
     */
    private fun buildEndSilhouettePath(
        silhouette: JigSilhouetteType,
        cx: Float,
        cy: Float,
        halfW: Float,
        halfH: Float
    ): Path {
        return Path().apply {
            when (silhouette) {
                JigSilhouetteType.ASYMMETRIC_HYDRO_KEEL -> {
                    // Asymmetric pentagonal keel
                    moveTo(cx, cy - halfH)
                    lineTo(cx + halfW, cy - halfH * 0.3f)
                    lineTo(cx + halfW * 0.5f, cy + halfH)
                    lineTo(cx - halfW * 0.5f, cy + halfH)
                    lineTo(cx - halfW, cy - halfH * 0.3f)
                    close()
                }

                JigSilhouetteType.SLOW_PITCH_DIAMOND -> {
                    // Symmetric lozenge diamond
                    moveTo(cx, cy - halfH)
                    lineTo(cx + halfW, cy)
                    lineTo(cx, cy + halfH)
                    lineTo(cx - halfW, cy)
                    close()
                }

                else -> {
                    // Streamlined oval teardrop
                    moveTo(cx, cy - halfH)
                    cubicTo(cx + halfW, cy - halfH * 0.5f, cx + halfW, cy + halfH * 0.5f, cx, cy + halfH)
                    cubicTo(cx - halfW, cy + halfH * 0.5f, cx - halfW, cy - halfH * 0.5f, cx, cy - halfH)
                    close()
                }
            }
        }
    }

    /**
     * Small 3D Axonometric Isometric Reference View of the Jig.
     */
    private fun drawIsometricJigPdf(
        canvas: Canvas,
        silhouette: JigSilhouetteType,
        cx: Float,
        cy: Float,
        length: Float,
        height: Float,
        fillPaint: Paint,
        outlinePaint: Paint
    ) {
        val isoPath = Path().apply {
            moveTo(cx - length * 0.8f, cy + 12f)
            lineTo(cx - length * 0.2f, cy - 8f)
            lineTo(cx + length * 0.8f, cy - 20f)
            lineTo(cx + length * 0.3f, cy + 5f)
            close()
        }
        canvas.drawPath(isoPath, fillPaint)
        canvas.drawPath(isoPath, outlinePaint)

        // Dorsal ridge
        canvas.drawLine(cx - length * 0.2f, cy - 8f, cx + length * 0.3f, cy + 5f, outlinePaint)
    }

    /**
     * Standard ISO First-Angle Projection Symbol (concentric circles + truncated cone).
     */
    private fun drawFirstAngleProjectionSymbol(canvas: Canvas, x: Float, y: Float) {
        val symPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        val clPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 0.5f
            pathEffect = DashPathEffect(floatArrayOf(4f, 2f), 0f)
            isAntiAlias = true
        }

        // Horizontal centerline
        canvas.drawLine(x - 5f, y + 10f, x + 50f, y + 10f, clPaint)

        // Truncated cone (Left)
        val cone = Path().apply {
            moveTo(x, y + 3f)
            lineTo(x + 18f, y)
            lineTo(x + 18f, y + 20f)
            lineTo(x, y + 17f)
            close()
        }
        canvas.drawPath(cone, symPaint)

        // Concentric Circles (Right)
        val circleCenterX = x + 34f
        val circleCenterY = y + 10f
        canvas.drawCircle(circleCenterX, circleCenterY, 5f, symPaint)
        canvas.drawCircle(circleCenterX, circleCenterY, 10f, symPaint)

        // Symbol label
        val lblPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 5.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("1ST ANGLE PROJECTION", x - 2f, y + 27f, lblPaint)
    }

    /**
     * Collision-aware Leader Callout with horizontal shoulder landing.
     */
    private fun drawLeaderCalloutPdf(
        canvas: Canvas,
        targetX: Float,
        targetY: Float,
        deltaX: Float,
        deltaY: Float,
        shoulderLength: Float,
        isLeft: Boolean,
        text: String
    ) {
        val leaderPaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            strokeWidth = 0.85f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            textSize = 6.2f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val kneeX = targetX + deltaX
        val kneeY = targetY + deltaY
        val endX = if (isLeft) kneeX - shoulderLength else kneeX + shoulderLength

        // Arrowhead dot at target
        canvas.drawCircle(targetX, targetY, 1.4f, Paint().apply { color = Color.rgb(2, 132, 199); style = Paint.Style.FILL; isAntiAlias = true })

        // Slanted leg & horizontal shoulder
        canvas.drawLine(targetX, targetY, kneeX, kneeY, leaderPaint)
        canvas.drawLine(kneeX, kneeY, endX, kneeY, leaderPaint)

        // Text placed safely above the shoulder
        val textX = if (isLeft) endX else kneeX + 2f
        canvas.drawText(text, textX, kneeY - 2.5f, textPaint)
    }

    /**
     * Parametric Dimension Line with Extension Lines, Inward-pointing Arrowheads, and Centered Text.
     */
    private fun drawParametricDimensionPdf(
        canvas: Canvas,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        dimOffset: Float,
        isHorizontal: Boolean,
        label: String
    ) {
        val dimPaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            strokeWidth = 0.85f
            isAntiAlias = true
        }
        val extPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            strokeWidth = 0.6f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            textSize = 7.2f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val arrowSize = 4.5f

        if (isHorizontal) {
            val dimY = y1 + dimOffset

            // Extension lines with 2pt clearance from object
            val extStartY1 = if (dimOffset < 0) y1 - 2f else y1 + 2f
            val extEndY1 = if (dimOffset < 0) dimY - 3f else dimY + 3f
            val extStartY2 = if (dimOffset < 0) y2 - 2f else y2 + 2f
            val extEndY2 = if (dimOffset < 0) dimY - 3f else dimY + 3f

            canvas.drawLine(x1, extStartY1, x1, extEndY1, extPaint)
            canvas.drawLine(x2, extStartY2, x2, extEndY2, extPaint)

            // Text width for center gap
            val textWidth = textPaint.measureText(label)
            val midX = (x1 + x2) / 2f
            val gap = textWidth / 2f + 4f

            // Dimension line split for text
            canvas.drawLine(x1, dimY, midX - gap, dimY, dimPaint)
            canvas.drawLine(midX + gap, dimY, x2, dimY, dimPaint)

            // Closed inward arrowheads
            drawArrowhead(canvas, x1, dimY, isPointingRight = true, arrowSize, dimPaint)
            drawArrowhead(canvas, x2, dimY, isPointingRight = false, arrowSize, dimPaint)

            // Centered text
            canvas.drawText(label, midX - textWidth / 2f, dimY + 2.5f, textPaint)
        } else {
            val dimX = x1 + dimOffset

            val extStartX1 = if (dimOffset < 0) x1 - 2f else x1 + 2f
            val extEndX1 = if (dimOffset < 0) dimX - 3f else dimX + 3f
            val extStartX2 = if (dimOffset < 0) x2 - 2f else x2 + 2f
            val extEndX2 = if (dimOffset < 0) dimX - 3f else dimX + 3f

            canvas.drawLine(extStartX1, y1, extEndX1, y1, extPaint)
            canvas.drawLine(extStartX2, y2, extEndX2, y2, extPaint)

            val textWidth = textPaint.measureText(label)
            val midY = (y1 + y2) / 2f
            val gap = 6f

            canvas.drawLine(dimX, y1, dimX, midY - gap, dimPaint)
            canvas.drawLine(dimX, midY + gap, dimX, y2, dimPaint)

            drawArrowheadVertical(canvas, dimX, y1, isPointingDown = true, arrowSize, dimPaint)
            drawArrowheadVertical(canvas, dimX, y2, isPointingDown = false, arrowSize, dimPaint)

            canvas.drawText(label, dimX + 3.5f, midY + 2.5f, textPaint)
        }
    }

    private fun drawArrowhead(canvas: Canvas, x: Float, y: Float, isPointingRight: Boolean, size: Float, paint: Paint) {
        val path = Path().apply {
            if (isPointingRight) {
                moveTo(x, y)
                lineTo(x + size, y - size * 0.45f)
                lineTo(x + size, y + size * 0.45f)
            } else {
                moveTo(x, y)
                lineTo(x - size, y - size * 0.45f)
                lineTo(x - size, y + size * 0.45f)
            }
            close()
        }
        val fill = Paint().apply { color = paint.color; style = Paint.Style.FILL; isAntiAlias = true }
        canvas.drawPath(path, fill)
    }

    private fun drawArrowheadVertical(canvas: Canvas, x: Float, y: Float, isPointingDown: Boolean, size: Float, paint: Paint) {
        val path = Path().apply {
            if (isPointingDown) {
                moveTo(x, y)
                lineTo(x - size * 0.45f, y + size)
                lineTo(x + size * 0.45f, y + size)
            } else {
                moveTo(x, y)
                lineTo(x - size * 0.45f, y - size)
                lineTo(x + size * 0.45f, y - size)
            }
            close()
        }
        val fill = Paint().apply { color = paint.color; style = Paint.Style.FILL; isAntiAlias = true }
        canvas.drawPath(path, fill)
    }

    /**
     * DYNAMIC CONFIGURATION SUMMARY & LEGEND TABLE (Section 24 & 25)
     * Reads directly from current configuration. Contains only currently selected values.
     */
    private fun drawDynamicLegendPdf(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        params: JigDrawingParameters
    ) {
        val bgPaint = Paint().apply { color = Color.rgb(248, 250, 252); style = Paint.Style.FILL; isAntiAlias = true }
        val borderPaint = Paint().apply { color = Color.rgb(203, 213, 225); style = Paint.Style.STROKE; strokeWidth = 0.8f; isAntiAlias = true }
        canvas.drawRect(x, y, x + width, y + height, bgPaint)
        canvas.drawRect(x, y, x + width, y + height, borderPaint)

        val headerPaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            textSize = 7f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CONFIGURATION SUMMARY & BOM SPECIFICATION", x + 6f, y + 10f, headerPaint)

        val labelPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 6.0f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }
        val valPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 6.2f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        // Two-column compact layout
        val col1X = x + 6f
        val col2X = x + width * 0.50f
        var rowY = y + 21f
        val rowH = 9.8f

        // Column 1
        canvas.drawText("PRODUCT: ${params.productName}", col1X, rowY, valPaint)
        canvas.drawText("MODEL: ${params.modelNumber}", col1X, rowY + rowH, valPaint)
        canvas.drawText("TARGET WEIGHT: ${params.weightGrams.toInt()} g", col1X, rowY + rowH * 2, valPaint)
        canvas.drawText("OVERALL LENGTH: ${params.lengthMm.toInt()} mm", col1X, rowY + rowH * 3, valPaint)
        canvas.drawText("MAX BODY WIDTH: ${params.widthMm.toInt()} mm", col1X, rowY + rowH * 4, valPaint)

        // Column 2
        canvas.drawText("MAIN COLOR: ${params.mainColor}", col2X, rowY, valPaint)
        canvas.drawText("SURFACE FINISH: ${params.finish}", col2X, rowY + rowH, valPaint)
        canvas.drawText("ASSIST HOOK: ${params.assistHook}", col2X, rowY + rowH * 2, valPaint)
        canvas.drawText("ASSIST CORD: ${params.assistCord}", col2X, rowY + rowH * 3, valPaint)
        canvas.drawText("RINGS: FR=${params.frontRing} | BR=${params.backRing}", col2X, rowY + rowH * 4, valPaint)
    }

    /**
     * DYNAMIC TITLE BLOCK (Section 38 & 39)
     */
    private fun drawDynamicTitleBlockPdf(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        params: JigDrawingParameters
    ) {
        val bgPaint = Paint().apply { color = Color.rgb(15, 23, 42); style = Paint.Style.FILL; isAntiAlias = true }
        canvas.drawRect(x, y, x + width, y + height, bgPaint)

        val brandPaint = Paint().apply {
            color = Color.rgb(56, 189, 248) // Sky 400
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.05f
        }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 5.8f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = Color.WHITE
            textSize = 6.0f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        canvas.drawText("7HOOKS GLOBAL TACKLE", x + 6f, y + 11f, brandPaint)
        canvas.drawText("MODEL: ${params.modelNumber}", x + 6f, y + 21f, boldPaint)
        canvas.drawText("DWG REF: ${params.drawingRefNumber}", x + 6f, y + 31f, textPaint)
        canvas.drawText("SCALE: 1:1 @ A4 | UNITS: mm, g", x + 6f, y + 41f, textPaint)
        canvas.drawText("TARGET MASS: ${params.weightGrams.toInt()} g | REV: 01", x + 6f, y + 51f, textPaint)
        canvas.drawText("STATUS: FOR REVIEW | DATE: $dateStr", x + 6f, y + 61f, textPaint)
    }

    data class Point2D(val x: Float, val y: Float)
}
