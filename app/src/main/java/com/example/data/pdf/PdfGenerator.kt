package com.example.data.pdf

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.PackagingConfiguration
import com.example.data.model.ProductConfiguration
import com.example.data.model.ConfigType
import com.example.data.model.JigPatternType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

object PdfGenerator {

    // Standard A4 dimensions in PostScript Points (72 dpi): 210mm x 297mm -> 595 x 842 points
    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842

    data class PdfValidationResult(
        val isValid: Boolean,
        val filePath: String = "",
        val fileSize: Long = 0,
        val file: File = File(filePath),
        val fileUri: Uri? = null,
        val errorMessage: String? = null
    )

    fun generateJigPdf(context: Context, config: ProductConfiguration): PdfValidationResult {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        try {
            // Draw background
            canvas.drawColor(Color.WHITE)

            // Outer technical border
            val borderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225) // Slate 300
                style = Paint.Style.STROKE
                strokeWidth = 1f
                isAntiAlias = true
            }
            canvas.drawRect(25f, 25f, (PAGE_WIDTH - 25).toFloat(), (PAGE_HEIGHT - 25).toFloat(), borderPaint)

            // Inner technical margin line
            borderPaint.strokeWidth = 0.5f
            borderPaint.color = Color.rgb(226, 232, 240)
            canvas.drawRect(28f, 28f, (PAGE_WIDTH - 28).toFloat(), (PAGE_HEIGHT - 28).toFloat(), borderPaint)

            // 1. TOP HEADER: 7Hooks Official Brand Logo (Vector Art directly rendered)
            drawBrandLogo(context, canvas, 40f, 38f, 120f, 40f)

            // Document Reference Box (Top Right)
            drawDocRefBox(canvas, PAGE_WIDTH - 200f, 40f, config.referenceNumber, "JIG SPECIFICATION")

            // 2. DOCUMENT TITLE
            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // Slate 900
                textSize = 15f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
                letterSpacing = 0.05f
            }
            canvas.drawText("JIG ENGINEERING SPECIFICATION", 40f, 105f, titlePaint)

            val subtitlePaint = Paint().apply {
                color = Color.rgb(2, 132, 199) // Sky 600
                textSize = 12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("${config.productName} — Model: ${config.modelNumber}", 40f, 122f, subtitlePaint)

            // Separator line
            val dividerPaint = Paint().apply {
                color = Color.rgb(2, 132, 199)
                strokeWidth = 1.5f
                isAntiAlias = true
            }
            canvas.drawLine(40f, 130f, (PAGE_WIDTH - 40).toFloat(), 130f, dividerPaint)

            // 3. ENGINEERING CAD DRAWING SECTION (Orthographic Views + CAD Frame)
            val drawingBoxTop = 138f
            val drawingBoxHeight = 246f
            drawEngineeringDrawingFrame(canvas, 40f, drawingBoxTop, (PAGE_WIDTH - 80).toFloat(), drawingBoxHeight, "CAD SCHEMATIC — ORTHOGRAPHIC PROJECTIONS (ELEVATION + PLAN + END VIEW)")
            
            // Draw CAD Drawing of the Jig
            drawJigCadDrawing(
                canvas = canvas,
                boxX = 40f,
                boxY = drawingBoxTop,
                boxWidth = (PAGE_WIDTH - 80).toFloat(),
                boxHeight = drawingBoxHeight,
                config = config
            )

            // 4. TECHNICAL SPECIFICATIONS TABLE
            val tableTop = drawingBoxTop + drawingBoxHeight + 12f
            drawSectionHeader(canvas, 40f, tableTop, "DETAILED MANUFACTURING SPECIFICATIONS")

            val specs = listOf(
                "Product & Model" to "${config.productName} (${config.modelNumber})",
                "Body Hydrofoil Shape" to "${config.shape} Head / Aerodynamic Keel Profile",
                "Primary Material & Grade" to "${config.material} (${config.densityGrade})",
                "Finished Target Mass" to "${config.weightGrams.toInt()} g (Tolerance: ${config.weightTolerance})",
                "Enveloping Dimensions" to "${config.lengthMm.toInt()} mm (L) × ${config.widthMm.toInt()} mm (W) × ${config.heightMm.toInt()} mm (H)",
                "Color Theme & Optical Finish" to "${config.colorName} • ${config.finishType} (${config.coating})",
                "Surface Pattern & Texture" to config.patternName,
                "Eye Specification" to "${config.eyeStyle} Dome Eye (${config.eyeShape} Pupil)",
                "Hook & Rigging Assembly" to "${config.hookSize} ${config.hookLevel} (${config.hookStyle})",
                "Additional Component" to config.additionalComponent,
                "Special Feature Layers" to listOfNotNull(
                    if (config.hasGlow) "Phosphorescent Glow" else null,
                    if (config.hasUvReactive) "UV Reactive" else null,
                    if (config.hasRattle) "Acoustic Rattle" else null,
                    if (config.hasWeedGuard) "Fiber Weed Guard" else null
                ).ifEmpty { listOf("None") }.joinToString(", ")
            )

            drawTable(canvas, 40f, tableTop + 14f, (PAGE_WIDTH - 80).toFloat(), specs)

            // 5. MANUFACTURING NOTES & TOLERANCE STATEMENT
            val notesTop = tableTop + 14f + (specs.size * 18f) + 10f
            drawSectionHeader(canvas, 40f, notesTop, "MANUFACTURING & QUALITY CONTROL NOTES")

            val bodyPaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 7.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }
            canvas.drawText("1. Continuous 304 Stainless Steel through-wire harness (Ø1.2mm) welded for minimum 45kg proof load.", 40f, notesTop + 13f, bodyPaint)
            canvas.drawText("2. Center of gravity calibrated with rear-bias weighting for high-speed vertical flutter and balance.", 40f, notesTop + 23f, bodyPaint)
            canvas.drawText("3. Surface finish: Robotized electrostatic basecoat + multi-stage UV marine lacquer (${config.coatingThickness}).", 40f, notesTop + 33f, bodyPaint)
            canvas.drawText("4. NOTICE: Tolerances unspecified in CAD default to factory standard (Linear: ${config.generalTolerance}, Mass: ${config.weightTolerance}). Mold draft angles to be verified by tooling vendor.", 40f, notesTop + 43f, bodyPaint)

            // 6. PROFESSIONAL MANUFACTURING TITLE BLOCK (ISO Standard)
            val titleBlockTop = notesTop + 52f
            val titleBlockHeight = 88f
            drawProfessionalManufacturingTitleBlock(
                canvas = canvas,
                x = 40f,
                y = titleBlockTop,
                width = (PAGE_WIDTH - 80).toFloat(),
                height = titleBlockHeight,
                config = config
            )

            // 7. FOOTER
            drawDocumentFooter(canvas, config.referenceNumber)

            document.finishPage(page)

            // Save file
            val sanitizedName = config.productName.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val fileName = "7Hooks_Jig_${sanitizedName}_${config.modelNumber}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            // QC Validation
            val validation = validatePdf(context, file, PAGE_WIDTH, PAGE_HEIGHT)
            return validation
        } catch (e: Exception) {
            document.close()
            return PdfValidationResult(isValid = false, errorMessage = "PDF Generation Error: ${e.localizedMessage}")
        }
    }

    fun generateRodPdf(context: Context, config: ProductConfiguration): PdfValidationResult {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        try {
            canvas.drawColor(Color.WHITE)

            // Outer technical border
            val borderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                style = Paint.Style.STROKE
                strokeWidth = 1f
                isAntiAlias = true
            }
            canvas.drawRect(25f, 25f, (PAGE_WIDTH - 25).toFloat(), (PAGE_HEIGHT - 25).toFloat(), borderPaint)

            // 1. TOP HEADER: 7Hooks Brand Logo only
            drawBrandLogo(context, canvas, 40f, 38f, 120f, 40f)

            // Document Ref Box
            drawDocRefBox(canvas, PAGE_WIDTH - 200f, 40f, config.referenceNumber, "ROD SPECIFICATION")

            // 2. TITLE
            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 15f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
                letterSpacing = 0.05f
            }
            canvas.drawText("ROD ENGINEERING SPECIFICATION", 40f, 105f, titlePaint)

            val subtitlePaint = Paint().apply {
                color = Color.rgb(2, 132, 199)
                textSize = 12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("${config.rodType} — Model: ${config.modelNumber}", 40f, 122f, subtitlePaint)

            val dividerPaint = Paint().apply {
                color = Color.rgb(2, 132, 199)
                strokeWidth = 1.5f
                isAntiAlias = true
            }
            canvas.drawLine(40f, 130f, (PAGE_WIDTH - 40).toFloat(), 130f, dividerPaint)

            // 3. ENGINEERING CAD DRAWING SECTION
            val drawingBoxTop = 140f
            val drawingBoxHeight = 250f
            drawEngineeringDrawingFrame(canvas, 40f, drawingBoxTop, (PAGE_WIDTH - 80).toFloat(), drawingBoxHeight, "ROD BLANK ARCHITECTURE & GUIDE LAYOUT")
            
            // Draw CAD Drawing of Rod
            drawRodCadDrawing(
                canvas = canvas,
                boxX = 40f,
                boxY = drawingBoxTop,
                boxWidth = (PAGE_WIDTH - 80).toFloat(),
                boxHeight = drawingBoxHeight,
                config = config
            )

            // 4. TECHNICAL SPECIFICATIONS TABLE
            val tableTop = drawingBoxTop + drawingBoxHeight + 15f
            drawSectionHeader(canvas, 40f, tableTop, "TECHNICAL SPECIFICATIONS")

            val lengthInMeters = String.format(Locale.US, "%.2f m (%d mm)", config.lengthMm / 1000f, config.lengthMm.toInt())
            val specs = listOf(
                "Rod Classification" to config.rodType,
                "Model Number" to config.modelNumber,
                "Overall Length" to lengthInMeters,
                "Handle / Grip Length" to "${config.handleLengthMm.toInt()} mm (High-Grade EVA/Cork)",
                "Blank Butt Diameter" to "${String.format(Locale.US, "%.1f", config.widthMm)} mm (Tip: 2.1 mm)",
                "Carbon Material" to config.material,
                "Power Rating" to config.power,
                "Action Curve" to config.action,
                "Number of Sections" to "${config.sections} Piece (Spigot Joint)",
                "Recommended Line" to config.recommendedLineWeight,
                "Recommended Lure" to config.recommendedLureWeight,
                "Maximum Deadlift Load" to "${String.format(Locale.US, "%.1f", config.maximumLoadKg)} kg"
            )

            drawTable(canvas, 40f, tableTop + 15f, (PAGE_WIDTH - 80).toFloat(), specs)

            // 5. MANDATORY LOAD VISUALIZATION DISCLAIMER
            val disclaimerTop = tableTop + 15f + (specs.size * 18f) + 15f
            drawSectionHeader(canvas, 40f, disclaimerTop, "ENGINEERING & LOAD DISCLAIMER")

            val disclaimerBoxPaint = Paint().apply {
                color = Color.rgb(254, 243, 199) // Amber 100
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(40f, disclaimerTop + 12f, (PAGE_WIDTH - 40).toFloat(), disclaimerTop + 46f, 4f, 4f, disclaimerBoxPaint)

            val disclaimerBorder = Paint().apply {
                color = Color.rgb(245, 158, 11) // Amber 500
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            canvas.drawRoundRect(40f, disclaimerTop + 12f, (PAGE_WIDTH - 40).toFloat(), disclaimerTop + 46f, 4f, 4f, disclaimerBorder)

            val disclaimerTextPaint = Paint().apply {
                color = Color.rgb(146, 64, 14) // Amber 900
                textSize = 7.8f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("NOTICE / DISCLAIMER:", 48f, disclaimerTop + 24f, disclaimerTextPaint)
            disclaimerTextPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText("\"Illustrative load visualization. Actual performance depends on material, construction, manufacturing", 48f, disclaimerTop + 34f, disclaimerTextPaint)
            canvas.drawText("tolerances and test conditions.\"", 48f, disclaimerTop + 43f, disclaimerTextPaint)

            // 6. FOOTER
            drawDocumentFooter(canvas, config.referenceNumber)

            document.finishPage(page)

            val sanitizedType = config.rodType.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val fileName = "7Hooks_Rod_${sanitizedType}_${config.modelNumber}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            return validatePdf(context, file, PAGE_WIDTH, PAGE_HEIGHT)
        } catch (e: Exception) {
            document.close()
            return PdfValidationResult(isValid = false, errorMessage = "PDF Generation Error: ${e.localizedMessage}")
        }
    }

    fun generateLurePdf(context: Context, config: ProductConfiguration): PdfValidationResult {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        try {
            canvas.drawColor(Color.WHITE)

            // Outer technical border
            val borderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                style = Paint.Style.STROKE
                strokeWidth = 1f
                isAntiAlias = true
            }
            canvas.drawRect(25f, 25f, (PAGE_WIDTH - 25).toFloat(), (PAGE_HEIGHT - 25).toFloat(), borderPaint)

            // Inner technical margin line
            borderPaint.strokeWidth = 0.5f
            borderPaint.color = Color.rgb(226, 232, 240)
            canvas.drawRect(28f, 28f, (PAGE_WIDTH - 28).toFloat(), (PAGE_HEIGHT - 28).toFloat(), borderPaint)

            // 1. TOP HEADER: 7Hooks Official Brand Logo
            drawBrandLogo(context, canvas, 40f, 38f, 120f, 40f)

            // Document Reference Box (Top Right)
            drawDocRefBox(canvas, PAGE_WIDTH - 200f, 40f, config.referenceNumber, "LURE SPECIFICATION")

            // 2. DOCUMENT TITLE
            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 15f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
                letterSpacing = 0.05f
            }
            canvas.drawText("LURE ENGINEERING SPECIFICATION", 40f, 105f, titlePaint)

            val subtitlePaint = Paint().apply {
                color = Color.rgb(2, 132, 199)
                textSize = 12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("${config.productName} — Model: ${config.modelNumber}", 40f, 122f, subtitlePaint)

            val dividerPaint = Paint().apply {
                color = Color.rgb(2, 132, 199)
                strokeWidth = 1.5f
                isAntiAlias = true
            }
            canvas.drawLine(40f, 130f, (PAGE_WIDTH - 40).toFloat(), 130f, dividerPaint)

            // 3. ENGINEERING CAD DRAWING SECTION
            val drawingBoxTop = 138f
            val drawingBoxHeight = 246f
            drawEngineeringDrawingFrame(canvas, 40f, drawingBoxTop, (PAGE_WIDTH - 80).toFloat(), drawingBoxHeight, "CAD SCHEMATIC — ORTHOGRAPHIC PROJECTIONS (ELEVATION + PLAN + END VIEW)")

            drawLureCadDrawing(
                canvas = canvas,
                boxX = 40f,
                boxY = drawingBoxTop,
                boxWidth = (PAGE_WIDTH - 80).toFloat(),
                boxHeight = drawingBoxHeight,
                config = config
            )

            // 4. TECHNICAL SPECIFICATIONS TABLE
            val tableTop = drawingBoxTop + drawingBoxHeight + 12f
            drawSectionHeader(canvas, 40f, tableTop, "DETAILED MANUFACTURING SPECIFICATIONS")

            val specs = listOf(
                "Product & Model" to "${config.productName} (${config.modelNumber})",
                "Lure Hydrofoil Class" to "${config.shape} • ${config.buoyancy}",
                "Body Material & Grade" to "${config.material} (${config.densityGrade})",
                "Target Weight" to "${String.format(Locale.US, "%.1f", config.weightGrams)} g (Tolerance: ${config.weightTolerance})",
                "Dimensions & Depth" to "${config.lengthMm.toInt()}L × ${config.widthMm.toInt()}W mm | Diving: ${String.format(Locale.US, "%.1f", config.divingDepthMeters)}m",
                "Color Theme & Finish" to "${config.colorName} • ${config.finishType} (${config.coating})",
                "Surface Pattern & Texture" to config.patternName,
                "Eye Specification" to "${config.eyeStyle} Dome Eye (${config.eyeShape} Pupil)",
                "Hook & Rigging Hardware" to "${config.hookQuantity}x ${config.hookSize} (${config.hookType})",
                "Buoyancy & Hydro Balance" to "${config.buoyancy} (Neutral Center-of-Gravity)",
                "Acoustics & Lighting" to listOfNotNull(
                    if (config.hasRattle) "Tungsten Acoustic Rattle" else null,
                    if (config.hasGlow) "Phosphorescent Glow" else null,
                    if (config.hasUvReactive) "UV Reactive Coat" else null
                ).ifEmpty { listOf("Standard Non-Acoustic") }.joinToString(", ")
            )

            drawTable(canvas, 40f, tableTop + 14f, (PAGE_WIDTH - 80).toFloat(), specs)

            // 5. MANUFACTURING NOTES & TOLERANCE STATEMENT
            val notesTop = tableTop + 14f + (specs.size * 18f) + 10f
            drawSectionHeader(canvas, 40f, notesTop, "MANUFACTURING & QUALITY CONTROL NOTES")

            val bodyPaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 7.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }
            canvas.drawText("1. Ultrasonic body seam weld tested under 0.8 MPa pneumatic pressure for watertight integrity.", 40f, notesTop + 13f, bodyPaint)
            canvas.drawText("2. Internal tungsten transfer weight chamber configured for extended casting trajectory and hydrodynamic pitch.", 40f, notesTop + 23f, bodyPaint)
            canvas.drawText("3. Surface protective coating: Automated electrostatic basecoat + UV marine topcoat (${config.coatingThickness}).", 40f, notesTop + 33f, bodyPaint)
            canvas.drawText("4. NOTICE: General Linear Tolerance: ${config.generalTolerance}. Finished Mass Tolerance: ${config.weightTolerance}. Tooling parting lines to be confirmed by manufacturer.", 40f, notesTop + 43f, bodyPaint)

            // 6. PROFESSIONAL TITLE BLOCK (ISO Standard)
            val titleBlockTop = notesTop + 52f
            val titleBlockHeight = 88f
            drawProfessionalManufacturingTitleBlock(
                canvas = canvas,
                x = 40f,
                y = titleBlockTop,
                width = (PAGE_WIDTH - 80).toFloat(),
                height = titleBlockHeight,
                config = config
            )

            // 7. FOOTER
            drawDocumentFooter(canvas, config.referenceNumber)

            document.finishPage(page)

            val sanitizedModel = config.modelNumber.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val fileName = "7Hooks_Lure_${sanitizedModel}_${config.referenceNumber}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            return validatePdf(context, file, PAGE_WIDTH, PAGE_HEIGHT)
        } catch (e: Exception) {
            document.close()
            return PdfValidationResult(isValid = false, errorMessage = "PDF Generation Error: ${e.localizedMessage}")
        }
    }

    fun generatePackagingPdf(context: Context, config: PackagingConfiguration): PdfValidationResult {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        try {
            canvas.drawColor(Color.WHITE)

            // Outer technical border
            val borderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                style = Paint.Style.STROKE
                strokeWidth = 1f
                isAntiAlias = true
            }
            canvas.drawRect(25f, 25f, (PAGE_WIDTH - 25).toFloat(), (PAGE_HEIGHT - 25).toFloat(), borderPaint)

            // 1. TOP HEADER: 7Hooks Brand Logo only
            drawBrandLogo(context, canvas, 40f, 38f, 120f, 40f)

            // Document Ref Box
            drawDocRefBox(canvas, PAGE_WIDTH - 200f, 40f, config.referenceNumber, "PACKAGING SPECIFICATION")

            // 2. TITLE
            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 15f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
                letterSpacing = 0.05f
            }
            canvas.drawText("CUSTOM PACKAGING SPECIFICATION", 40f, 105f, titlePaint)

            val subtitlePaint = Paint().apply {
                color = Color.rgb(2, 132, 199)
                textSize = 12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("${config.packagingType} Package — Model: ${config.modelNumber}", 40f, 122f, subtitlePaint)

            val dividerPaint = Paint().apply {
                color = Color.rgb(2, 132, 199)
                strokeWidth = 1.5f
                isAntiAlias = true
            }
            canvas.drawLine(40f, 130f, (PAGE_WIDTH - 40).toFloat(), 130f, dividerPaint)

            // 3. PACKAGING 2.5D MOCKUP SECTION
            val drawingBoxTop = 140f
            val drawingBoxHeight = 250f
            drawEngineeringDrawingFrame(canvas, 40f, drawingBoxTop, (PAGE_WIDTH - 80).toFloat(), drawingBoxHeight, "PACKAGING 3D/2.5D PROJECTION & BRANDING LAYOUT")
            
            // Draw Packaging Mockup
            drawPackagingMockupCad(
                context = context,
                canvas = canvas,
                boxX = 40f,
                boxY = drawingBoxTop,
                boxWidth = (PAGE_WIDTH - 80).toFloat(),
                boxHeight = drawingBoxHeight,
                config = config
            )

            // 4. SPECIFICATIONS TABLE
            val tableTop = drawingBoxTop + drawingBoxHeight + 15f
            drawSectionHeader(canvas, 40f, tableTop, "PACKAGING SPECIFICATIONS & PRINT DETAILS")

            val specs = listOf(
                "Packaging Format" to config.packagingType,
                "Product Line" to config.productName,
                "Model Reference" to config.modelNumber,
                "Client / Brand" to config.companyName,
                "Dimensions (H x W x D)" to config.packagingDimensions,
                "Material Grade" to config.packagingMaterial,
                "Color / Coating" to config.packagingColor,
                "Print Process" to "6-Color Offset + Spot Matte UV & Gold Foil Stamping",
                "Contact Info" to "${config.contactEmail} | ${config.contactPhone}",
                "Official Website" to config.contactWebsite
            )

            drawTable(canvas, 40f, tableTop + 15f, (PAGE_WIDTH - 80).toFloat(), specs)

            // 5. PACKAGING NOTES
            val notesTop = tableTop + 15f + (specs.size * 18f) + 15f
            drawSectionHeader(canvas, 40f, notesTop, "ARTWORK & LOGISTIC NOTES")

            val bodyPaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }
            canvas.drawText("• Client Artwork Logo verified and positioned on primary display panel (PDP) with high-density ink absorption.", 40f, notesTop + 16f, bodyPaint)
            canvas.drawText("• Standard euro-slot hang hole incorporated on top tab for retail peg display compliance.", 40f, notesTop + 28f, bodyPaint)
            canvas.drawText("• Custom Notes: ${config.packagingNotes}", 40f, notesTop + 40f, bodyPaint)

            // 6. FOOTER
            drawDocumentFooter(canvas, config.referenceNumber)

            document.finishPage(page)

            val fileName = "7Hooks_Packaging_${config.modelNumber}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            return validatePdf(context, file, PAGE_WIDTH, PAGE_HEIGHT)
        } catch (e: Exception) {
            document.close()
            return PdfValidationResult(isValid = false, errorMessage = "PDF Generation Error: ${e.localizedMessage}")
        }
    }

    // Programmatic verification of PDF output
    private fun validatePdf(context: Context, file: File, expectedWidth: Int, expectedHeight: Int): PdfValidationResult {
        if (!file.exists() || file.length() == 0L) {
            return PdfValidationResult(isValid = false, errorMessage = "Generated PDF file is empty or missing.")
        }
        val uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        }
        return PdfValidationResult(
            isValid = true,
            filePath = file.absolutePath,
            fileSize = file.length(),
            file = file,
            fileUri = uri
        )
    }

    fun sharePdf(context: Context, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File not found for sharing", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "7Hooks Specification Document: ${file.name}")
                putExtra(Intent.EXTRA_TEXT, "Attached is the official 7Hooks Product Engineering Specification Document.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share 7Hooks Specification PDF via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun viewPdf(context: Context, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "PDF saved to app storage (${file.name}). Use Share to export.", Toast.LENGTH_LONG).show()
        }
    }

    // --- Vector CAD / Blueprint Drawing Helpers for PDF ---

    private fun drawBrandLogo(context: Context, canvas: Canvas, x: Float, y: Float, width: Float, height: Float) {
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, com.example.R.drawable.official_7hooks_logo)
        if (drawable != null) {
            drawable.setBounds(x.toInt(), y.toInt(), (x + width).toInt(), (y + height).toInt())
            drawable.draw(canvas)
        } else {
            // Render 7Hooks Vector Symbol as fallback
            val hookPaint = Paint().apply {
                color = Color.rgb(2, 132, 199) // Sky 600
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val orangePaint = Paint().apply {
                color = Color.rgb(234, 88, 12) // Orange 600
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            val path7 = Path().apply {
                moveTo(x + 5f, y + 5f)
                lineTo(x + 35f, y + 5f)
                lineTo(x + 24f, y + 35f)
                lineTo(x + 18f, y + 33f)
                lineTo(x + 26f, y + 12f)
                lineTo(x + 5f, y + 12f)
                close()
            }
            canvas.drawPath(path7, hookPaint)

            val hookCurve = Path().apply {
                moveTo(x + 16f, y + 15f)
                quadTo(x + 8f, y + 25f, x + 16f, y + 34f)
                quadTo(x + 24f, y + 34f, x + 24f, y + 28f)
                lineTo(x + 28f, y + 26f)
                lineTo(x + 22f, y + 38f)
                quadTo(x + 12f, y + 38f, x + 4f, y + 26f)
                quadTo(x + 4f, y + 15f, x + 16f, y + 15f)
                close()
            }
            canvas.drawPath(hookCurve, orangePaint)
        }
    }

    private fun drawDocRefBox(canvas: Canvas, x: Float, y: Float, refNum: String, docType: String) {
        val boxPaint = Paint().apply {
            color = Color.rgb(241, 245, 249) // Slate 100
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(x, y, x + 160f, y + 42f, 4f, 4f, boxPaint)

        val borderPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        canvas.drawRoundRect(x, y, x + 160f, y + 42f, 4f, 4f, borderPaint)

        val labelPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 6.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("DOCUMENT REF:", x + 8f, y + 13f, labelPaint)
        canvas.drawText("ISSUE DATE:", x + 8f, y + 32f, labelPaint)

        val valPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(refNum, x + 72f, y + 14f, valPaint)

        val dateStr = SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(Date())
        valPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        valPaint.textSize = 7.5f
        canvas.drawText(dateStr, x + 72f, y + 32f, valPaint)
    }

    private fun drawEngineeringDrawingFrame(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, title: String) {
        // Grid background for CAD feeling
        val bgPaint = Paint().apply {
            color = Color.rgb(250, 250, 252)
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + width, y + height, bgPaint)

        // Subtle grid lines
        val gridPaint = Paint().apply {
            color = Color.rgb(235, 240, 245)
            strokeWidth = 0.5f
        }
        var gx = x
        while (gx <= x + width) {
            canvas.drawLine(gx, y, gx, y + height, gridPaint)
            gx += 20f
        }
        var gy = y
        while (gy <= y + height) {
            canvas.drawLine(x, gy, x + width, gy, gridPaint)
            gy += 20f
        }

        // Frame border
        val framePaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        canvas.drawRect(x, y, x + width, y + height, framePaint)

        // Title Tag
        val tagBg = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + 240f, y + 16f, tagBg)

        val tagText = Paint().apply {
            color = Color.WHITE
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.05f
        }
        canvas.drawText(title, x + 8f, y + 11.5f, tagText)
    }

    private fun drawCenterOfGravitySymbol(canvas: Canvas, cx: Float, cy: Float, label: String = "CG") {
        val r = 6f
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, r, bgPaint)

        val blackPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val rectF = RectF(cx - r, cy - r, cx + r, cy + r)
        canvas.drawArc(rectF, 0f, 90f, true, blackPaint)
        canvas.drawArc(rectF, 180f, 90f, true, blackPaint)

        val strokePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, r, strokePaint)
        canvas.drawLine(cx - r - 2f, cy, cx + r + 2f, cy, strokePaint)
        canvas.drawLine(cx, cy - r - 2f, cx, cy + r + 2f, strokePaint)

        val textPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 6f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(label, cx + r + 2f, cy + 2f, textPaint)
    }

    private fun drawProfessionalManufacturingTitleBlock(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        config: ProductConfiguration
    ) {
        val borderPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        canvas.drawRect(x, y, x + width, y + height, borderPaint)

        val linePaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            isAntiAlias = true
        }

        val labelPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 5.2f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val valPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 7f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val monoValPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 7f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        // Header banner
        val headerBg = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + width, y + 16f, headerBg)
        canvas.drawLine(x, y + 16f, x + width, y + 16f, borderPaint)

        val companyPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.05f
        }
        canvas.drawText("7HOOKS PRECISION MARINE TACKLE CO.  |  MANUFACTURING & TOOLING DRAWING", x + 8f, y + 11f, companyPaint)

        // Row 1: Product & Model / Drawing No
        canvas.drawLine(x, y + 34f, x + width, y + 34f, linePaint)
        val colSplit1 = x + width * 0.46f
        canvas.drawLine(colSplit1, y + 16f, colSplit1, y + 34f, linePaint)

        canvas.drawText("PRODUCT / MODEL NAME:", x + 6f, y + 23f, labelPaint)
        canvas.drawText("${config.productName} (${config.modelNumber})", x + 6f, y + 31.5f, valPaint)

        canvas.drawText("DRAWING NO. & REVISION:", colSplit1 + 6f, y + 23f, labelPaint)
        val dwgNum = "${config.effectiveDrawingNumber}-REV-${config.revision}"
        canvas.drawText(dwgNum, colSplit1 + 6f, y + 31.5f, monoValPaint)

        // Row 2: Metadata (Scale, Units, Status, Revision, Date)
        canvas.drawLine(x, y + 52f, x + width, y + 52f, linePaint)
        val colW = width / 5f
        for (i in 1..4) {
            canvas.drawLine(x + colW * i, y + 34f, x + colW * i, y + 52f, linePaint)
        }

        canvas.drawText("SCALE:", x + 4f, y + 41f, labelPaint)
        canvas.drawText("1:1 FULL", x + 4f, y + 49f, valPaint)

        canvas.drawText("UNITS:", x + colW + 4f, y + 41f, labelPaint)
        canvas.drawText("METRIC (mm/g)", x + colW + 4f, y + 49f, valPaint)

        canvas.drawText("STATUS:", x + colW * 2 + 4f, y + 41f, labelPaint)
        val statusColor = if (config.drawingStatus == "APPROVED") Color.rgb(22, 101, 52) else Color.rgb(2, 132, 199)
        canvas.drawText(config.drawingStatus, x + colW * 2 + 4f, y + 49f, Paint(valPaint).apply { color = statusColor })

        canvas.drawText("REVISION:", x + colW * 3 + 4f, y + 41f, labelPaint)
        canvas.drawText("REV ${config.revision}", x + colW * 3 + 4f, y + 49f, monoValPaint)

        val dateFormatted = SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(Date(config.timestamp))
        canvas.drawText("DATE:", x + colW * 4 + 4f, y + 41f, labelPaint)
        canvas.drawText(dateFormatted, x + colW * 4 + 4f, y + 49f, valPaint)

        // Row 3: Tolerances & Material
        canvas.drawLine(x, y + 68f, x + width, y + 68f, linePaint)
        val tolSplit = x + width * 0.52f
        canvas.drawLine(tolSplit, y + 52f, tolSplit, y + 68f, linePaint)

        canvas.drawText("STANDARD TOLERANCES:", x + 6f, y + 59f, labelPaint)
        canvas.drawText("LINEAR: ${config.generalTolerance} | MASS: ${config.weightTolerance} | ANGULAR: ±0.5°", x + 6f, y + 66f, monoValPaint)

        canvas.drawText("MATERIAL / DATUM:", tolSplit + 6f, y + 59f, labelPaint)
        canvas.drawText("${config.material} (${config.densityGrade}) | ${config.datumA}", tolSplit + 6f, y + 66f, valPaint)

        // Row 4: Approvals
        val appW = width / 4f
        for (i in 1..3) {
            canvas.drawLine(x + appW * i, y + 68f, x + appW * i, y + height, linePaint)
        }

        canvas.drawText("DESIGNED BY:", x + 4f, y + 76f, labelPaint)
        canvas.drawText("7Hooks Studio", x + 4f, y + 84f, valPaint)

        canvas.drawText("CHECKED BY:", x + appW + 4f, y + 76f, labelPaint)
        canvas.drawText("[ TBD / FACTORY ]", x + appW + 4f, y + 84f, labelPaint)

        canvas.drawText("APPROVED BY:", x + appW * 2 + 4f, y + 76f, labelPaint)
        canvas.drawText("[ TBD / PRODUCTION ]", x + appW * 2 + 4f, y + 84f, labelPaint)

        canvas.drawText("FACTORY QC:", x + appW * 3 + 4f, y + 76f, labelPaint)
        canvas.drawText("ISO 9001 VERIFIED", x + appW * 3 + 4f, y + 84f, valPaint)
    }

    private fun drawJigCadDrawing(canvas: Canvas, boxX: Float, boxY: Float, boxWidth: Float, boxHeight: Float, config: ProductConfiguration) {
        val centerLinePaint = Paint().apply {
            color = Color.rgb(239, 68, 68)
            strokeWidth = 0.7f
            pathEffect = DashPathEffect(floatArrayOf(10f, 3f, 2f, 3f), 0f)
            isAntiAlias = true
        }

        val outlinePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }

        val viewTagPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 6f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        // -------------------------------------------------------------
        // VIEW A: SIDE ELEVATION (Upper area: boxY + 20 to boxY + 140)
        // -------------------------------------------------------------
        canvas.drawText("VIEW A: SIDE ELEVATION (1:1 SCALE)", boxX + 12f, boxY + 24f, viewTagPaint)

        val elevCenterX = boxX + boxWidth * 0.40f
        val elevCenterY = boxY + 76f

        val scaleL = (config.lengthMm / 160f).coerceIn(0.55f, 1.25f)
        val scaleH = (config.heightMm / 25f).coerceIn(0.55f, 1.25f)
        val jigDrawLength = 170f * scaleL
        val jigDrawHeight = 36f * scaleH

        val halfL = jigDrawLength / 2f
        val halfH = jigDrawHeight / 2f

        // Centerline for Elevation
        canvas.drawLine(elevCenterX - halfL - 28f, elevCenterY, elevCenterX + halfL + 28f, elevCenterY, centerLinePaint)

        // Body Profile Side Elevation
        val bodyPath = Path().apply {
            moveTo(elevCenterX - halfL, elevCenterY)
            cubicTo(
                elevCenterX - halfL * 0.65f, elevCenterY - halfH * 0.95f,
                elevCenterX + halfL * 0.1f, elevCenterY - halfH * 1.05f,
                elevCenterX + halfL * 0.6f, elevCenterY - halfH * 0.65f
            )
            lineTo(elevCenterX + halfL, elevCenterY)
            cubicTo(
                elevCenterX + halfL * 0.6f, elevCenterY + halfH * 0.65f,
                elevCenterX + halfL * 0.1f, elevCenterY + halfH * 1.05f,
                elevCenterX - halfL * 0.65f, elevCenterY + halfH * 0.95f
            )
            close()
        }

        // CAD Gradient fill
        val fillPaint = Paint().apply {
            shader = LinearGradient(
                elevCenterX, elevCenterY - halfH,
                elevCenterX, elevCenterY + halfH,
                config.baseColorHex.toInt(),
                config.accentColorHex.toInt(),
                Shader.TileMode.CLAMP
            )
            isAntiAlias = true
            alpha = 175
        }
        canvas.drawPath(bodyPath, fillPaint)
        canvas.drawPath(bodyPath, outlinePaint)

        // Lateral keel line
        val keelPaint = Paint().apply {
            color = Color.rgb(255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1.1f
            isAntiAlias = true
        }
        canvas.drawLine(elevCenterX - halfL + 12f, elevCenterY, elevCenterX + halfL - 10f, elevCenterY, keelPaint)

        // Front Eyelet (Through-wire line tie)
        val ringPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
            isAntiAlias = true
        }
        val ringFill = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(elevCenterX - halfL - 6f, elevCenterY, 4.5f, ringFill)
        canvas.drawCircle(elevCenterX - halfL - 6f, elevCenterY, 4.5f, ringPaint)
        canvas.drawCircle(elevCenterX - halfL - 6f, elevCenterY, 1.8f, outlinePaint)

        // Rear Eyelet
        canvas.drawCircle(elevCenterX + halfL + 6f, elevCenterY, 4.5f, ringFill)
        canvas.drawCircle(elevCenterX + halfL + 6f, elevCenterY, 4.5f, ringPaint)
        canvas.drawCircle(elevCenterX + halfL + 6f, elevCenterY, 1.8f, outlinePaint)

        // 3D Lure Eye
        val eyeX = elevCenterX - halfL + 18f * scaleL
        val eyeY = elevCenterY - 4f
        canvas.drawCircle(eyeX, eyeY, 4f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL; isAntiAlias = true })
        canvas.drawCircle(eyeX, eyeY, 4f, outlinePaint)
        canvas.drawCircle(eyeX + 0.8f, eyeY, 2f, Paint().apply { color = Color.rgb(15, 23, 42); style = Paint.Style.FILL; isAntiAlias = true })

        // Center of Gravity Marker (60/40 rear bias)
        val cgX = elevCenterX + halfL * 0.18f
        drawCenterOfGravitySymbol(canvas, cgX, elevCenterY, "CG 60/40")

        // Callout: Line Tie
        val calloutPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 5.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawLine(elevCenterX - halfL - 6f, elevCenterY - 4.5f, elevCenterX - halfL - 20f, elevCenterY - 22f, outlinePaint)
        canvas.drawLine(elevCenterX - halfL - 20f, elevCenterY - 22f, elevCenterX - halfL - 45f, elevCenterY - 22f, outlinePaint)
        canvas.drawText("LINE-TIE Ø1.2mm 304SS", elevCenterX - halfL - 45f, elevCenterY - 24f, calloutPaint)

        // Callout: Hook Specification
        canvas.drawLine(elevCenterX + halfL + 6f, elevCenterY + 4.5f, elevCenterX + halfL + 18f, elevCenterY + 22f, outlinePaint)
        canvas.drawLine(elevCenterX + halfL + 18f, elevCenterY + 22f, elevCenterX + halfL + 40f, elevCenterY + 22f, outlinePaint)
        canvas.drawText("${config.hookSize} ${config.hookStyle}", elevCenterX + halfL + 18f, elevCenterY + 29f, calloutPaint)

        // Dimensions for Elevation
        drawDimensionLine(
            canvas = canvas,
            x1 = elevCenterX - halfL - 10f,
            y1 = elevCenterY - halfH - 18f,
            x2 = elevCenterX + halfL + 10f,
            y2 = elevCenterY - halfH - 18f,
            extY1 = elevCenterY,
            extY2 = elevCenterY,
            label = "OVERALL LENGTH = ${config.lengthMm.toInt()} mm"
        )

        drawVerticalDimensionLine(
            canvas = canvas,
            y1 = elevCenterY - halfH,
            x1 = elevCenterX + halfL + 25f,
            y2 = elevCenterY + halfH,
            x2 = elevCenterX + halfL + 25f,
            extX1 = elevCenterX,
            extX2 = elevCenterX,
            label = "H = ${config.heightMm.toInt()} mm"
        )

        // -------------------------------------------------------------
        // VIEW B: TOP PLAN PROJECTION (Lower left: boxY + 152 to boxY + 235)
        // -------------------------------------------------------------
        canvas.drawText("VIEW B: PLAN / TOP PROJECTION (DATUM X-X)", boxX + 12f, boxY + 152f, viewTagPaint)

        val planCenterY = boxY + 190f
        val planHalfW = (config.widthMm / 28f).coerceIn(0.55f, 1.25f) * 16f

        // Centerline for Plan View
        canvas.drawLine(elevCenterX - halfL - 28f, planCenterY, elevCenterX + halfL + 28f, planCenterY, centerLinePaint)

        // Plan View symmetric contour
        val planPath = Path().apply {
            moveTo(elevCenterX - halfL, planCenterY)
            cubicTo(
                elevCenterX - halfL * 0.5f, planCenterY - planHalfW,
                elevCenterX + halfL * 0.2f, planCenterY - planHalfW * 0.9f,
                elevCenterX + halfL, planCenterY
            )
            cubicTo(
                elevCenterX + halfL * 0.2f, planCenterY + planHalfW * 0.9f,
                elevCenterX - halfL * 0.5f, planCenterY + planHalfW,
                elevCenterX - halfL, planCenterY
            )
            close()
        }
        canvas.drawPath(planPath, fillPaint)
        canvas.drawPath(planPath, outlinePaint)

        drawVerticalDimensionLine(
            canvas = canvas,
            y1 = planCenterY - planHalfW,
            x1 = elevCenterX + halfL + 25f,
            y2 = planCenterY + planHalfW,
            x2 = elevCenterX + halfL + 25f,
            extX1 = elevCenterX,
            extX2 = elevCenterX,
            label = "WIDTH = ${config.widthMm.toInt()} mm"
        )

        // -------------------------------------------------------------
        // VIEW C: TRANSVERSE HYDROFOIL SECTION (Right side: boxX + 410 to boxX + 490)
        // -------------------------------------------------------------
        val secX = boxX + boxWidth - 62f
        val secY = boxY + 95f
        canvas.drawText("SECTION C-C", secX - 25f, boxY + 24f, viewTagPaint)
        canvas.drawText("TRANSVERSE", secX - 25f, boxY + 32f, viewTagPaint)

        // Centerlines for Section
        canvas.drawLine(secX - 25f, secY, secX + 25f, secY, centerLinePaint)
        canvas.drawLine(secX, secY - 30f, secX, secY + 30f, centerLinePaint)

        // Diamond/hydrofoil section
        val secPath = Path().apply {
            moveTo(secX, secY - jigDrawHeight * 0.45f)
            lineTo(secX + planHalfW, secY)
            lineTo(secX, secY + jigDrawHeight * 0.45f)
            lineTo(secX - planHalfW, secY)
            close()
        }
        canvas.drawPath(secPath, fillPaint)
        canvas.drawPath(secPath, outlinePaint)

        // Hatching lines for section (CAD section hatch)
        val hatchPaint = Paint().apply {
            color = Color.argb(80, 15, 23, 42)
            strokeWidth = 0.6f
        }
        for (h in -2..2) {
            val hy = secY + h * 6f
            canvas.drawLine(secX - 8f, hy - 4f, secX + 8f, hy + 4f, hatchPaint)
        }
        canvas.drawText("DATUM Y-Y", secX - 18f, secY + 38f, calloutPaint)

        // Bottom CAD Parameter Callout Strip
        val paramBg = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(boxX + 12f, boxY + boxHeight - 24f, boxX + 260f, boxY + boxHeight - 6f, 3f, 3f, paramBg)

        val paramText = Paint().apply {
            color = Color.rgb(56, 189, 248)
            textSize = 6.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("MASS: ${config.weightGrams.toInt()}g (${config.weightTolerance}) | MAT: ${config.material}", boxX + 18f, boxY + boxHeight - 12f, paramText)
    }

    private fun drawLureCadDrawing(canvas: Canvas, boxX: Float, boxY: Float, boxWidth: Float, boxHeight: Float, config: ProductConfiguration) {
        val centerLinePaint = Paint().apply {
            color = Color.rgb(239, 68, 68)
            strokeWidth = 0.7f
            pathEffect = DashPathEffect(floatArrayOf(10f, 3f, 2f, 3f), 0f)
            isAntiAlias = true
        }

        val outlinePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }

        val viewTagPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 6f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        // -------------------------------------------------------------
        // VIEW A: SIDE ELEVATION (Upper area)
        // -------------------------------------------------------------
        canvas.drawText("VIEW A: SIDE ELEVATION & HYDRODYNAMICS (1:1 SCALE)", boxX + 12f, boxY + 24f, viewTagPaint)

        val elevCenterX = boxX + boxWidth * 0.40f
        val elevCenterY = boxY + 76f

        val scaleL = (config.lengthMm / 150f).coerceIn(0.55f, 1.25f)
        val scaleW = (config.widthMm / 25f).coerceIn(0.55f, 1.25f)
        val lureDrawLength = 175f * scaleL
        val lureDrawHeight = 36f * scaleW

        val halfL = lureDrawLength / 2f
        val halfH = lureDrawHeight / 2f

        canvas.drawLine(elevCenterX - halfL - 28f, elevCenterY, elevCenterX + halfL + 28f, elevCenterY, centerLinePaint)

        val bodyPath = Path().apply {
            moveTo(elevCenterX - halfL, elevCenterY)
            cubicTo(
                elevCenterX - halfL * 0.7f, elevCenterY - halfH * 0.95f,
                elevCenterX - halfL * 0.1f, elevCenterY - halfH * 1.05f,
                elevCenterX + halfL * 0.6f, elevCenterY - halfH * 0.5f
            )
            lineTo(elevCenterX + halfL, elevCenterY)
            cubicTo(
                elevCenterX + halfL * 0.6f, elevCenterY + halfH * 0.5f,
                elevCenterX - halfL * 0.1f, elevCenterY + halfH * 0.9f,
                elevCenterX - halfL * 0.7f, elevCenterY + halfH * 0.7f
            )
            close()
        }

        val fillPaint = Paint().apply {
            shader = LinearGradient(
                elevCenterX, elevCenterY - halfH,
                elevCenterX, elevCenterY + halfH,
                config.baseColorHex.toInt(),
                config.accentColorHex.toInt(),
                Shader.TileMode.CLAMP
            )
            isAntiAlias = true
            alpha = 175
        }
        canvas.drawPath(bodyPath, fillPaint)
        canvas.drawPath(bodyPath, outlinePaint)

        // Diving Lip / Bib
        val lipPath = Path().apply {
            moveTo(elevCenterX - halfL + 4f, elevCenterY + 2f)
            lineTo(elevCenterX - halfL - 18f * scaleL, elevCenterY + 18f * scaleL)
            lineTo(elevCenterX - halfL - 11f * scaleL, elevCenterY + 22f * scaleL)
            lineTo(elevCenterX - halfL + 9f, elevCenterY + 7f)
            close()
        }
        val lipPaint = Paint().apply {
            color = Color.argb(160, 226, 232, 240)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawPath(lipPath, lipPaint)
        canvas.drawPath(lipPath, outlinePaint)

        // Lateral Line
        val lateralPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 1.1f
            isAntiAlias = true
        }
        canvas.drawLine(elevCenterX - halfL + 12f, elevCenterY - 2f, elevCenterX + halfL - 10f, elevCenterY, lateralPaint)

        // Belly Hanger & Tail Hanger Rings
        val ringPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
            isAntiAlias = true
        }
        canvas.drawCircle(elevCenterX - 8f, elevCenterY + halfH * 0.85f + 4f, 4f, ringPaint)
        canvas.drawCircle(elevCenterX + halfL + 6f, elevCenterY, 4f, ringPaint)

        // 3D Lure Eye
        val eyeX = elevCenterX - halfL + 18f * scaleL
        val eyeY = elevCenterY - 4f
        canvas.drawCircle(eyeX, eyeY, 4f, Paint().apply { color = Color.WHITE; style = Paint.Style.FILL; isAntiAlias = true })
        canvas.drawCircle(eyeX, eyeY, 4f, outlinePaint)
        canvas.drawCircle(eyeX + 0.8f, eyeY, 2f, Paint().apply { color = Color.rgb(15, 23, 42); style = Paint.Style.FILL; isAntiAlias = true })

        // Center of Gravity Marker (Neutral balance)
        drawCenterOfGravitySymbol(canvas, elevCenterX - halfL * 0.05f, elevCenterY, "CG NEUTRAL")

        // Dimensions
        drawDimensionLine(
            canvas = canvas,
            x1 = elevCenterX - halfL - 10f,
            y1 = elevCenterY - halfH - 18f,
            x2 = elevCenterX + halfL + 10f,
            y2 = elevCenterY - halfH - 18f,
            extY1 = elevCenterY,
            extY2 = elevCenterY,
            label = "OVERALL LENGTH = ${config.lengthMm.toInt()} mm"
        )

        drawVerticalDimensionLine(
            canvas = canvas,
            y1 = elevCenterY - halfH,
            x1 = elevCenterX + halfL + 25f,
            y2 = elevCenterY + halfH,
            x2 = elevCenterX + halfL + 25f,
            extX1 = elevCenterX,
            extX2 = elevCenterX,
            label = "H = ${config.heightMm.toInt()} mm"
        )

        // Callout: Bib Angle & Diving depth
        val calloutPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 5.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawLine(elevCenterX - halfL - 14f, elevCenterY + 20f, elevCenterX - halfL - 25f, elevCenterY + 34f, outlinePaint)
        canvas.drawLine(elevCenterX - halfL - 25f, elevCenterY + 34f, elevCenterX - halfL - 50f, elevCenterY + 34f, outlinePaint)
        canvas.drawText("DEPTH: ${String.format(Locale.US, "%.1f", config.divingDepthMeters)}m", elevCenterX - halfL - 50f, elevCenterY + 32f, calloutPaint)

        // -------------------------------------------------------------
        // VIEW B: TOP PLAN PROJECTION
        // -------------------------------------------------------------
        canvas.drawText("VIEW B: PLAN / TOP PROJECTION (DATUM X-X)", boxX + 12f, boxY + 152f, viewTagPaint)

        val planCenterY = boxY + 190f
        val planHalfW = (config.widthMm / 25f).coerceIn(0.55f, 1.25f) * 14f

        canvas.drawLine(elevCenterX - halfL - 28f, planCenterY, elevCenterX + halfL + 28f, planCenterY, centerLinePaint)

        val planPath = Path().apply {
            moveTo(elevCenterX - halfL, planCenterY)
            cubicTo(
                elevCenterX - halfL * 0.45f, planCenterY - planHalfW,
                elevCenterX + halfL * 0.2f, planCenterY - planHalfW * 0.8f,
                elevCenterX + halfL, planCenterY
            )
            cubicTo(
                elevCenterX + halfL * 0.2f, planCenterY + planHalfW * 0.8f,
                elevCenterX - halfL * 0.45f, planCenterY + planHalfW,
                elevCenterX - halfL, planCenterY
            )
            close()
        }
        canvas.drawPath(planPath, fillPaint)
        canvas.drawPath(planPath, outlinePaint)

        drawVerticalDimensionLine(
            canvas = canvas,
            y1 = planCenterY - planHalfW,
            x1 = elevCenterX + halfL + 25f,
            y2 = planCenterY + planHalfW,
            x2 = elevCenterX + halfL + 25f,
            extX1 = elevCenterX,
            extX2 = elevCenterX,
            label = "WIDTH = ${config.widthMm.toInt()} mm"
        )

        // -------------------------------------------------------------
        // VIEW C: TRANSVERSE FRONT / BIB SECTION
        // -------------------------------------------------------------
        val secX = boxX + boxWidth - 62f
        val secY = boxY + 95f
        canvas.drawText("VIEW C: FRONT", secX - 25f, boxY + 24f, viewTagPaint)
        canvas.drawText("DIVING BIB", secX - 25f, boxY + 32f, viewTagPaint)

        canvas.drawLine(secX - 25f, secY, secX + 25f, secY, centerLinePaint)
        canvas.drawLine(secX, secY - 30f, secX, secY + 30f, centerLinePaint)

        val bibPath = Path().apply {
            moveTo(secX - planHalfW * 1.1f, secY + 6f)
            lineTo(secX + planHalfW * 1.1f, secY + 6f)
            lineTo(secX + planHalfW * 0.7f, secY + 22f)
            lineTo(secX - planHalfW * 0.7f, secY + 22f)
            close()
        }
        canvas.drawPath(bibPath, lipPaint)
        canvas.drawPath(bibPath, outlinePaint)

        // Upper body oval in front view
        val frontBody = RectF(secX - planHalfW * 0.85f, secY - 20f, secX + planHalfW * 0.85f, secY + 6f)
        canvas.drawOval(frontBody, fillPaint)
        canvas.drawOval(frontBody, outlinePaint)
        canvas.drawCircle(secX, secY - 4f, 2.5f, ringPaint)

        canvas.drawText("DATUM Y-Y", secX - 18f, secY + 34f, calloutPaint)

        // Bottom CAD Parameter Callout Strip
        val paramBg = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(boxX + 12f, boxY + boxHeight - 24f, boxX + 280f, boxY + boxHeight - 6f, 3f, 3f, paramBg)

        val paramText = Paint().apply {
            color = Color.rgb(56, 189, 248)
            textSize = 6.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("MASS: ${String.format(Locale.US, "%.1f", config.weightGrams)}g (${config.weightTolerance}) | ${config.hookQuantity}x ${config.hookType}", boxX + 18f, boxY + boxHeight - 12f, paramText)
    }

    private fun drawRodCadDrawing(canvas: Canvas, boxX: Float, boxY: Float, boxWidth: Float, boxHeight: Float, config: ProductConfiguration) {
        val startX = boxX + 30f
        val endX = boxX + boxWidth - 30f
        val centerY = boxY + boxHeight * 0.5f

        // Centerline
        val centerLinePaint = Paint().apply {
            color = Color.rgb(239, 68, 68)
            strokeWidth = 0.8f
            pathEffect = DashPathEffect(floatArrayOf(12f, 4f, 2f, 4f), 0f)
            isAntiAlias = true
        }
        canvas.drawLine(startX - 15f, centerY, endX + 15f, centerY, centerLinePaint)

        // 1. Handle & Butt Section
        val buttLength = (endX - startX) * 0.22f
        val buttRadius = (config.widthMm * 0.7f).coerceIn(6f, 14f)

        // EVA / Cork Split Grip Handle
        val handlePaint = Paint().apply {
            color = Color.rgb(30, 41, 59) // Slate 800
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        // Rear Grip
        canvas.drawRoundRect(startX, centerY - buttRadius, startX + buttLength * 0.4f, centerY + buttRadius, 3f, 3f, handlePaint)
        // Reel Seat Collar
        val reelSeatPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(startX + buttLength * 0.45f, centerY - buttRadius * 1.1f, startX + buttLength * 0.75f, centerY + buttRadius * 1.1f, 2f, 2f, reelSeatPaint)
        // Fore Grip
        canvas.drawRoundRect(startX + buttLength * 0.8f, centerY - buttRadius * 0.85f, startX + buttLength, centerY + buttRadius * 0.85f, 2f, 2f, handlePaint)

        // 2. Tapered Carbon Blank (from handle to tip)
        val blankPath = Path().apply {
            moveTo(startX + buttLength, centerY - buttRadius * 0.6f)
            lineTo(endX, centerY - 1.5f) // tip top
            lineTo(endX, centerY + 1.5f)
            lineTo(startX + buttLength, centerY + buttRadius * 0.6f)
            close()
        }
        val blankPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawPath(blankPath, blankPaint)

        // Blank Cross-Weave pattern
        val weavePaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        var wx = startX + buttLength
        while (wx <= startX + buttLength + 120f) {
            canvas.drawLine(wx, centerY - 4f, wx + 6f, centerY + 4f, weavePaint)
            canvas.drawLine(wx + 6f, centerY - 4f, wx, centerY + 4f, weavePaint)
            wx += 12f
        }

        // 3. Graduated Guides
        val guideCount = 7
        val guidePaint = Paint().apply {
            color = Color.rgb(234, 88, 12) // Blaze orange / chrome guides
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        val blankLength = endX - (startX + buttLength)
        for (i in 1..guideCount) {
            val ratio = (i.toFloat() / (guideCount + 1)).let { it * it } // progressive spacing
            val gx = startX + buttLength + blankLength * ratio
            val gHeight = (16f - i * 1.8f).coerceAtLeast(4f)
            // Guide leg and ring
            canvas.drawLine(gx, centerY - 2f, gx - 3f, centerY - 2f - gHeight, guidePaint)
            canvas.drawCircle(gx - 3f, centerY - 2f - gHeight, (gHeight * 0.35f).coerceAtLeast(1.5f), guidePaint)
        }

        // Tip Top Guide
        canvas.drawCircle(endX + 2f, centerY, 3f, guidePaint)

        // 4. Section joint indicator
        if (config.sections > 1) {
            val sectionX = startX + (endX - startX) * 0.52f
            val ferrulePaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 2f
            }
            canvas.drawLine(sectionX, centerY - 8f, sectionX, centerY + 8f, ferrulePaint)
            val calloutPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 6.5f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }
            canvas.drawText("SPIGOT JOINT", sectionX - 20f, centerY + 20f, calloutPaint)
        }

        // 5. Dimension lines
        drawDimensionLine(
            canvas = canvas,
            x1 = startX,
            y1 = centerY - 45f,
            x2 = endX,
            y2 = centerY - 45f,
            extY1 = centerY,
            extY2 = centerY,
            label = "TOTAL BLANK LENGTH = ${config.lengthMm.toInt()} mm"
        )

        drawDimensionLine(
            canvas = canvas,
            x1 = startX,
            y1 = centerY + 38f,
            x2 = startX + buttLength,
            y2 = centerY + 38f,
            extY1 = centerY,
            extY2 = centerY,
            label = "HANDLE = ${config.handleLengthMm.toInt()} mm"
        )

        // Specification Badge
        val badgeBg = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(boxX + 12f, boxY + boxHeight - 48f, boxX + 210f, boxY + boxHeight - 12f, 4f, 4f, badgeBg)

        val badgeText = Paint().apply {
            color = Color.rgb(56, 189, 248)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("POWER: ${config.power} | ACTION: ${config.action}", boxX + 18f, boxY + boxHeight - 34f, badgeText)
        badgeText.color = Color.WHITE
        canvas.drawText("MAX LOAD: ${String.format(Locale.US, "%.1f", config.maximumLoadKg)} kg | SECTIONS: ${config.sections}pc", boxX + 18f, boxY + boxHeight - 20f, badgeText)
    }

    private fun drawPackagingMockupCad(context: Context, canvas: Canvas, boxX: Float, boxY: Float, boxWidth: Float, boxHeight: Float, config: PackagingConfiguration) {
        val centerX = boxX + boxWidth * 0.45f
        val centerY = boxY + boxHeight * 0.5f

        val pw = 140f
        val ph = 180f
        val depth = 45f

        // 2.5D Isometric Packaging Box
        // 1. Top Face
        val topPath = Path().apply {
            moveTo(centerX, centerY - ph / 2)
            lineTo(centerX + depth * 0.8f, centerY - ph / 2 - depth * 0.5f)
            lineTo(centerX + pw + depth * 0.8f, centerY - ph / 2 - depth * 0.5f)
            lineTo(centerX + pw, centerY - ph / 2)
            close()
        }
        val topPaint = Paint().apply {
            color = Color.rgb(14, 165, 233) // Sky 500
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawPath(topPath, topPaint)

        // 2. Right Side Face (Depth)
        val sidePath = Path().apply {
            moveTo(centerX + pw, centerY - ph / 2)
            lineTo(centerX + pw + depth * 0.8f, centerY - ph / 2 - depth * 0.5f)
            lineTo(centerX + pw + depth * 0.8f, centerY + ph / 2 - depth * 0.5f)
            lineTo(centerX + pw, centerY + ph / 2)
            close()
        }
        val sidePaint = Paint().apply {
            color = Color.rgb(3, 105, 161) // Sky 700
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawPath(sidePath, sidePaint)

        // 3. Front Face (Primary Display Panel)
        val frontRect = RectF(centerX, centerY - ph / 2, centerX + pw, centerY + ph / 2)
        val frontPaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(frontRect, frontPaint)

        // Front Face Brand Header
        val brandHeader = Paint().apply {
            color = Color.rgb(2, 132, 199)
            style = Paint.Style.FILL
        }
        canvas.drawRect(centerX, centerY - ph / 2, centerX + pw, centerY - ph / 2 + 40f, brandHeader)

        // 7Hooks mini logo on front
        val logoText = Paint().apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("7HOOKS", centerX + 12f, centerY - ph / 2 + 25f, logoText)

        // Customer Company Name / Uploaded branding on box
        val compText = Paint().apply {
            color = Color.rgb(249, 115, 22) // Orange 500
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(config.companyName.take(20), centerX + 12f, centerY - ph / 2 + 62f, compText)

        val prodText = Paint().apply {
            color = Color.WHITE
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText(config.productName.take(22), centerX + 12f, centerY - ph / 2 + 78f, prodText)

        // Window cut-out / Blister presentation
        val windowPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(centerX + 12f, centerY - ph / 2 + 90f, centerX + pw - 12f, centerY + ph / 2 - 20f, 4f, 4f, windowPaint)

        val windowBorder = Paint().apply {
            color = Color.rgb(56, 189, 248)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(centerX + 12f, centerY - ph / 2 + 90f, centerX + pw - 12f, centerY + ph / 2 - 20f, 4f, 4f, windowBorder)

        // Hang Tab on top
        val tabPath = Path().apply {
            moveTo(centerX + pw * 0.35f, centerY - ph / 2)
            lineTo(centerX + pw * 0.35f, centerY - ph / 2 - 22f)
            lineTo(centerX + pw * 0.65f, centerY - ph / 2 - 22f)
            lineTo(centerX + pw * 0.65f, centerY - ph / 2)
            close()
        }
        val tabPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawPath(tabPath, tabPaint)
        // Euro slot hole
        val slotPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(centerX + pw * 0.42f, centerY - ph / 2 - 16f, centerX + pw * 0.58f, centerY - ph / 2 - 8f, 3f, 3f, slotPaint)

        // Outline borders
        val cadOutline = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        canvas.drawPath(topPath, cadOutline)
        canvas.drawPath(sidePath, cadOutline)
        canvas.drawRect(frontRect, cadOutline)

        // Dimension callouts
        drawDimensionLine(
            canvas = canvas,
            x1 = centerX,
            y1 = centerY + ph / 2 + 25f,
            x2 = centerX + pw,
            y2 = centerY + ph / 2 + 25f,
            extY1 = centerY + ph / 2,
            extY2 = centerY + ph / 2,
            label = "WIDTH: ${config.packagingDimensions.substringBefore("x").trim()}"
        )
    }

    private fun drawSectionHeader(canvas: Canvas, x: Float, y: Float, title: String) {
        val barPaint = Paint().apply {
            color = Color.rgb(2, 132, 199)
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + 3.5f, y + 10f, barPaint)

        val headerPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.05f
        }
        canvas.drawText(title, x + 8f, y + 8.5f, headerPaint)
    }

    private fun drawTable(canvas: Canvas, x: Float, y: Float, width: Float, rows: List<Pair<String, String>>) {
        val rowHeight = 16f
        val col1Width = width * 0.38f

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.75f
        }
        val altBgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        val labelPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val valPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        // Table Header
        canvas.drawRect(x, y, x + width, y + rowHeight, Paint().apply { color = Color.rgb(241, 245, 249) })
        canvas.drawText("PARAMETER", x + 6f, y + 11f, labelPaint)
        canvas.drawText("ENGINEERING SPECIFICATION VALUE", x + col1Width + 6f, y + 11f, labelPaint)
        canvas.drawLine(x, y + rowHeight, x + width, y + rowHeight, linePaint)

        var curY = y + rowHeight
        rows.forEachIndexed { index, (key, value) ->
            if (index % 2 == 1) {
                canvas.drawRect(x, curY, x + width, curY + rowHeight, altBgPaint)
            }
            canvas.drawText(key, x + 6f, curY + 11f, labelPaint)
            canvas.drawText(value, x + col1Width + 6f, curY + 11f, valPaint)
            canvas.drawLine(x, curY + rowHeight, x + width, curY + rowHeight, linePaint)
            curY += rowHeight
        }

        // Outer box for table
        val border = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        canvas.drawRect(x, y, x + width, curY, border)
        canvas.drawLine(x + col1Width, y, x + col1Width, curY, linePaint)
    }

    private fun drawDocumentFooter(canvas: Canvas, refNum: String) {
        val y = (PAGE_HEIGHT - 38).toFloat()

        val dividerPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 0.75f
        }
        canvas.drawLine(40f, y, (PAGE_WIDTH - 40).toFloat(), y, dividerPaint)

        val footerText = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("7Hooks Precision Tackle Engineering — Proprietary & Confidential Technical Specification Sheet", 40f, y + 14f, footerText)
        canvas.drawText("DOC REF: $refNum | PAGE 1 OF 1 (A4 FORMAT)", (PAGE_WIDTH - 210).toFloat(), y + 14f, footerText)
    }

    private fun drawDimensionLine(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, extY1: Float, extY2: Float, label: String) {
        val linePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        // Extension lines
        canvas.drawLine(x1, extY1, x1, y1 - 4f, linePaint)
        canvas.drawLine(x2, extY2, x2, y2 - 4f, linePaint)

        // Main dimension line
        canvas.drawLine(x1, y1, x2, y2, linePaint)

        // Arrowheads
        drawArrowhead(canvas, x1, y1, isPointingRight = false)
        drawArrowhead(canvas, x2, y2, isPointingRight = true)

        // Label
        val textPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(label, (x1 + x2) / 2f, y1 - 4f, textPaint)
    }

    private fun drawVerticalDimensionLine(canvas: Canvas, y1: Float, x1: Float, y2: Float, x2: Float, extX1: Float, extX2: Float, label: String) {
        val linePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        // Extension lines
        canvas.drawLine(extX1, y1, x1 + 4f, y1, linePaint)
        canvas.drawLine(extX2, y2, x2 + 4f, y2, linePaint)

        // Main line
        canvas.drawLine(x1, y1, x2, y2, linePaint)

        // Arrows
        drawVerticalArrowhead(canvas, x1, y1, isPointingDown = false)
        drawVerticalArrowhead(canvas, x2, y2, isPointingDown = true)

        val textPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 7f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(label, x1 + 6f, (y1 + y2) / 2f + 2.5f, textPaint)
    }

    private fun drawArrowhead(canvas: Canvas, x: Float, y: Float, isPointingRight: Boolean) {
        val path = Path()
        val dir = if (isPointingRight) -1 else 1
        path.moveTo(x, y)
        path.lineTo(x + dir * 6f, y - 2.5f)
        path.lineTo(x + dir * 6f, y + 2.5f)
        path.close()
        val paint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawPath(path, paint)
    }

    private fun drawVerticalArrowhead(canvas: Canvas, x: Float, y: Float, isPointingDown: Boolean) {
        val path = Path()
        val dir = if (isPointingDown) -1 else 1
        path.moveTo(x, y)
        path.lineTo(x - 2.5f, y + dir * 6f)
        path.lineTo(x + 2.5f, y + dir * 6f)
        path.close()
        val paint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawPath(path, paint)
    }
}
