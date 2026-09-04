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

            // 3. ENGINEERING CAD DRAWING SECTION
            val drawingBoxTop = 140f
            val drawingBoxHeight = 270f
            drawEngineeringDrawingFrame(canvas, 40f, drawingBoxTop, (PAGE_WIDTH - 80).toFloat(), drawingBoxHeight, "TECHNICAL DRAWING & DIMENSIONAL PROJECTION")
            
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
            val tableTop = drawingBoxTop + drawingBoxHeight + 15f
            drawSectionHeader(canvas, 40f, tableTop, "TECHNICAL SPECIFICATIONS")

            val specs = listOf(
                "Product Name" to config.productName,
                "Model Number" to config.modelNumber,
                "Category / Action" to config.category,
                "Primary Material" to config.material,
                "Target Weight" to "${config.weightGrams.toInt()} g (±0.5g tolerance)",
                "Overall Length" to "${config.lengthMm.toInt()} mm",
                "Max Body Width" to "${config.widthMm.toInt()} mm",
                "Color Theme" to config.colorName,
                "Eyelet Construction" to "Integrated Solid Stainless Steel Through-Wire (1.2mm)",
                "Surface Treatment" to "Multi-layer UV Luminous & Holographic Hard Coat"
            )

            drawTable(canvas, 40f, tableTop + 15f, (PAGE_WIDTH - 80).toFloat(), specs)

            // 5. MANUFACTURING & PRODUCT NOTES
            val notesTop = tableTop + 15f + (specs.size * 18f) + 20f
            drawSectionHeader(canvas, 40f, notesTop, "MANUFACTURING & QUALITY CONTROL NOTES")

            val bodyPaint = Paint().apply {
                color = Color.rgb(51, 65, 85) // Slate 700
                textSize = 8.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }
            canvas.drawText("• Precision hydrodynamic balance optimized for rapid vertical descent and high-frequency flutter on retrieve.", 40f, notesTop + 16f, bodyPaint)
            canvas.drawText("• Center of gravity weighted 60/40 rear-bias for aerodynamic long-distance casting and strike triggering.", 40f, notesTop + 28f, bodyPaint)
            canvas.drawText("• Saltwater corrosion resistance: 500-hour ASTM B117 salt-spray tested without degradation.", 40f, notesTop + 40f, bodyPaint)
            canvas.drawText("• Configuration certified for automated CNC die casting and robotized electrostatic lacquer application.", 40f, notesTop + 52f, bodyPaint)

            // 6. FOOTER
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
            val drawingBoxTop = 140f
            val drawingBoxHeight = 270f
            drawEngineeringDrawingFrame(canvas, 40f, drawingBoxTop, (PAGE_WIDTH - 80).toFloat(), drawingBoxHeight, "CAD SCHEMATIC & HYDRODYNAMIC PROJECTION")

            drawLureCadDrawing(
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

            val specs = listOf(
                "Product Name" to config.productName,
                "Model Number" to config.modelNumber,
                "Lure Category" to config.category,
                "Body Material" to config.material,
                "Target Weight" to "${String.format(Locale.US, "%.1f", config.weightGrams)} g (±0.5g tolerance)",
                "Overall Length" to "${config.lengthMm.toInt()} mm",
                "Max Body Width" to "${config.widthMm.toInt()} mm",
                "Color Theme" to config.colorName,
                "Diving Depth" to "${String.format(Locale.US, "%.1f", config.divingDepthMeters)} meters",
                "Hook Assembly" to config.hookType.ifEmpty { "#4 BKK Heavy Treble" },
                "Buoyancy Action" to config.buoyancy.ifEmpty { "Suspending" }
            )

            drawTable(canvas, 40f, tableTop + 15f, (PAGE_WIDTH - 80).toFloat(), specs)

            // 5. MANUFACTURING TOLERANCES
            val tolTop = tableTop + 15f + (specs.size * 18f) + 15f
            drawSectionHeader(canvas, 40f, tolTop, "MANUFACTURING TOLERANCES & HYDRODYNAMICS")

            val tolerances = listOf(
                "Dimensional Tolerance" to "±0.15 mm (CNC Resin / ABS Injection)",
                "Weight Accuracy" to "±0.50 grams (Tungsten internal weight balance)",
                "Hardware Grade" to "Heavy-Duty Stainless Steel Split Rings & Wire-Through Keel",
                "Finish Application" to "Multi-Layer UV High-Gloss Automotive Topcoat"
            )
            drawTable(canvas, 40f, tolTop + 15f, (PAGE_WIDTH - 80).toFloat(), tolerances)

            // 6. FOOTER
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

    private fun drawJigCadDrawing(canvas: Canvas, boxX: Float, boxY: Float, boxWidth: Float, boxHeight: Float, config: ProductConfiguration) {
        val centerX = boxX + boxWidth * 0.45f
        val centerY = boxY + boxHeight * 0.52f

        // Calculate scaled dimensions for CAD drawing
        val scaleLength = (config.lengthMm / 200f).coerceIn(0.6f, 1.4f)
        val scaleWidth = (config.widthMm / 30f).coerceIn(0.6f, 1.4f)
        val jigDrawLength = 220f * scaleLength
        val jigDrawWidth = 46f * scaleWidth

        val halfL = jigDrawLength / 2f
        val halfW = jigDrawWidth / 2f

        // 1. Centerlines (Dash-dot technical lines)
        val centerLinePaint = Paint().apply {
            color = Color.rgb(239, 68, 68) // Red 500
            strokeWidth = 0.8f
            pathEffect = DashPathEffect(floatArrayOf(12f, 4f, 2f, 4f), 0f)
            isAntiAlias = true
        }
        canvas.drawLine(centerX - halfL - 40f, centerY, centerX + halfL + 40f, centerY, centerLinePaint)
        canvas.drawLine(centerX, centerY - halfW - 35f, centerX, centerY + halfW + 35f, centerLinePaint)

        // 2. Jig Main Body Profile (Horizontal projection)
        val bodyPath = Path().apply {
            moveTo(centerX - halfL, centerY) // Front nose eyelet base
            // Top aerodynamic curve
            cubicTo(
                centerX - halfL * 0.6f, centerY - halfW * 0.85f,
                centerX + halfL * 0.1f, centerY - halfW,
                centerX + halfL * 0.6f, centerY - halfW * 0.7f
            )
            lineTo(centerX + halfL, centerY) // Tail eyelet base
            // Bottom hydrodynamic curve
            cubicTo(
                centerX + halfL * 0.6f, centerY + halfW * 0.7f,
                centerX + halfL * 0.1f, centerY + halfW,
                centerX - halfL * 0.6f, centerY + halfW * 0.85f
            )
            close()
        }

        // Fill with subtle gradient reflecting selected color
        val fillPaint = Paint().apply {
            shader = LinearGradient(
                centerX, centerY - halfW,
                centerX, centerY + halfW,
                config.baseColorHex.toInt(),
                config.accentColorHex.toInt(),
                Shader.TileMode.CLAMP
            )
            isAntiAlias = true
            alpha = 180
        }
        canvas.drawPath(bodyPath, fillPaint)

        // Body outline (CAD crisp stroke)
        val outlinePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawPath(bodyPath, outlinePaint)

        // Lateral keel / facet line
        val facetPaint = Paint().apply {
            color = Color.rgb(255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        canvas.drawLine(centerX - halfL + 10f, centerY, centerX + halfL - 10f, centerY, facetPaint)

        // Holographic slashes or dots depending on pattern
        when (config.patternType) {
            JigPatternType.DOT_PATTERN -> {
                val dotPaint = Paint().apply {
                    color = config.accentColorHex.toInt()
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                for (i in -3..3) {
                    val dx = centerX + i * 18f * scaleLength
                    canvas.drawCircle(dx, centerY - 6f, 2.5f, dotPaint)
                    canvas.drawCircle(dx + 9f, centerY + 6f, 2.5f, dotPaint)
                }
            }
            else -> {
                val slashPaint = Paint().apply {
                    color = Color.argb(120, 255, 255, 255)
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    isAntiAlias = true
                }
                for (i in -4..4) {
                    val sx = centerX + i * 16f * scaleLength
                    canvas.drawLine(sx - 8f, centerY - 14f, sx + 8f, centerY + 14f, slashPaint)
                }
            }
        }

        // Eyelet Rings (Stainless Steel Rings)
        val ringPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val ringFill = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        // Front eyelet
        canvas.drawCircle(centerX - halfL - 7f, centerY, 5f, ringFill)
        canvas.drawCircle(centerX - halfL - 7f, centerY, 5f, ringPaint)
        canvas.drawCircle(centerX - halfL - 7f, centerY, 2f, outlinePaint)

        // Rear eyelet
        canvas.drawCircle(centerX + halfL + 7f, centerY, 5f, ringFill)
        canvas.drawCircle(centerX + halfL + 7f, centerY, 5f, ringPaint)
        canvas.drawCircle(centerX + halfL + 7f, centerY, 2f, outlinePaint)

        // 3D Lure Eye
        val eyeBase = Paint().apply {
            color = Color.rgb(255, 255, 255)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val eyePupil = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val eyeX = centerX - halfL + 25f * scaleLength
        val eyeY = centerY - 5f
        canvas.drawCircle(eyeX, eyeY, 4.5f, eyeBase)
        canvas.drawCircle(eyeX, eyeY, 4.5f, outlinePaint)
        canvas.drawCircle(eyeX + 1f, eyeY, 2.2f, eyePupil)

        // 3. DIMENSION LINES & CALLOUTS
        // Overall Length Dimension (Top)
        val dimY = centerY - halfW - 22f
        drawDimensionLine(
            canvas = canvas,
            x1 = centerX - halfL - 12f,
            y1 = dimY,
            x2 = centerX + halfL + 12f,
            y2 = dimY,
            extY1 = centerY,
            extY2 = centerY,
            label = "LENGTH = ${config.lengthMm.toInt()} mm"
        )

        // Width Dimension (Right)
        val dimX = centerX + halfL + 35f
        drawVerticalDimensionLine(
            canvas = canvas,
            y1 = centerY - halfW,
            x1 = dimX,
            y2 = centerY + halfW,
            x2 = dimX,
            extX1 = centerX,
            extX2 = centerX,
            label = "WIDTH = ${config.widthMm.toInt()} mm"
        )

        // Weight & Model Callout Badge (Bottom Left)
        val badgeBg = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(boxX + 12f, boxY + boxHeight - 48f, boxX + 160f, boxY + boxHeight - 12f, 4f, 4f, badgeBg)

        val badgeText = Paint().apply {
            color = Color.rgb(56, 189, 248) // Sky 400
            textSize = 7.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("MASS: ${config.weightGrams.toInt()}g | MAT: ${config.material}", boxX + 18f, boxY + boxHeight - 34f, badgeText)
        badgeText.color = Color.WHITE
        canvas.drawText("SCALE: 1:1 ENGINEERING VIEW", boxX + 18f, boxY + boxHeight - 20f, badgeText)
    }

    private fun drawLureCadDrawing(canvas: Canvas, boxX: Float, boxY: Float, boxWidth: Float, boxHeight: Float, config: ProductConfiguration) {
        val centerX = boxX + boxWidth * 0.45f
        val centerY = boxY + boxHeight * 0.52f

        val scaleLength = (config.lengthMm / 160f).coerceIn(0.6f, 1.4f)
        val scaleWidth = (config.widthMm / 26f).coerceIn(0.6f, 1.4f)
        val lureDrawLength = 220f * scaleLength
        val lureDrawWidth = 46f * scaleWidth

        val halfL = lureDrawLength / 2f
        val halfW = lureDrawWidth / 2f

        // Centerlines
        val centerLinePaint = Paint().apply {
            color = Color.rgb(239, 68, 68)
            strokeWidth = 0.8f
            pathEffect = DashPathEffect(floatArrayOf(12f, 4f, 2f, 4f), 0f)
            isAntiAlias = true
        }
        canvas.drawLine(centerX - halfL - 40f, centerY, centerX + halfL + 40f, centerY, centerLinePaint)
        canvas.drawLine(centerX, centerY - halfW - 35f, centerX, centerY + halfW + 35f, centerLinePaint)

        // Lure Main Body Profile
        val bodyPath = Path().apply {
            moveTo(centerX - halfL, centerY)
            cubicTo(
                centerX - halfL * 0.7f, centerY - halfW * 0.95f,
                centerX - halfL * 0.1f, centerY - halfW * 1.05f,
                centerX + halfL * 0.6f, centerY - halfW * 0.5f
            )
            lineTo(centerX + halfL, centerY)
            cubicTo(
                centerX + halfL * 0.6f, centerY + halfW * 0.5f,
                centerX - halfL * 0.1f, centerY + halfW * 0.9f,
                centerX - halfL * 0.7f, centerY + halfW * 0.7f
            )
            close()
        }

        val fillPaint = Paint().apply {
            shader = LinearGradient(
                centerX, centerY - halfW,
                centerX, centerY + halfW,
                config.baseColorHex.toInt(),
                config.accentColorHex.toInt(),
                Shader.TileMode.CLAMP
            )
            isAntiAlias = true
            alpha = 180
        }
        canvas.drawPath(bodyPath, fillPaint)

        val outlinePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawPath(bodyPath, outlinePaint)

        // Diving Lip / Bib
        val lipPath = Path().apply {
            moveTo(centerX - halfL + 4f, centerY + 3f)
            lineTo(centerX - halfL - 22f * scaleLength, centerY + 24f * scaleLength)
            lineTo(centerX - halfL - 14f * scaleLength, centerY + 28f * scaleLength)
            lineTo(centerX - halfL + 10f, centerY + 9f)
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
            color = Color.rgb(255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        canvas.drawLine(centerX - halfL + 12f, centerY - 2f, centerX + halfL - 10f, centerY, lateralPaint)

        // Belly Hanger & Tail Hanger
        val ringPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
            isAntiAlias = true
        }
        canvas.drawCircle(centerX - 8f, centerY + halfW * 0.85f + 4f, 4.5f, ringPaint)
        canvas.drawCircle(centerX + halfL + 6f, centerY, 4.5f, ringPaint)

        // 3D Lure Eye
        val eyeBase = Paint().apply {
            color = Color.rgb(255, 255, 255)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val eyePupil = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val eyeX = centerX - halfL + 20f * scaleLength
        val eyeY = centerY - 5f
        canvas.drawCircle(eyeX, eyeY, 4.5f, eyeBase)
        canvas.drawCircle(eyeX, eyeY, 4.5f, outlinePaint)
        canvas.drawCircle(eyeX + 1f, eyeY, 2.2f, eyePupil)

        // Dimensions
        val dimY = centerY - halfW - 22f
        drawDimensionLine(
            canvas = canvas,
            x1 = centerX - halfL - 12f,
            y1 = dimY,
            x2 = centerX + halfL + 12f,
            y2 = dimY,
            extY1 = centerY,
            extY2 = centerY,
            label = "LENGTH = ${config.lengthMm.toInt()} mm"
        )

        val dimX = centerX + halfL + 35f
        drawVerticalDimensionLine(
            canvas = canvas,
            y1 = centerY - halfW,
            x1 = dimX,
            y2 = centerY + halfW,
            x2 = dimX,
            extX1 = centerX,
            extX2 = centerX,
            label = "WIDTH = ${config.widthMm.toInt()} mm"
        )

        // Specification Badge
        val badgeBg = Paint().apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(boxX + 12f, boxY + boxHeight - 48f, boxX + 175f, boxY + boxHeight - 12f, 4f, 4f, badgeBg)

        val badgeText = Paint().apply {
            color = Color.rgb(56, 189, 248)
            textSize = 7.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("MASS: ${String.format(Locale.US, "%.1f", config.weightGrams)}g | DEPTH: ${String.format(Locale.US, "%.1f", config.divingDepthMeters)}m", boxX + 18f, boxY + boxHeight - 34f, badgeText)
        badgeText.color = Color.WHITE
        canvas.drawText("HOOK: ${config.hookType.ifEmpty { "#4 Heavy Treble" }}", boxX + 18f, boxY + boxHeight - 20f, badgeText)
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
