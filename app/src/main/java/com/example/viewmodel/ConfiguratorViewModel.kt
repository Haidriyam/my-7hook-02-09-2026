package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiImageService
import com.example.data.local.AppDatabase
import com.example.data.local.SavedConfigEntity
import com.example.data.model.*
import com.example.data.pdf.PdfGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConfiguratorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val configDao = db.savedConfigDao()
    private val geminiService = GeminiImageService(application)

    // PROGRESSIVE JIG CONFIGURATOR STATE (Shape-First)
    private val _currentJigConfig = MutableStateFlow(
        JigConfiguration.fromShape(JigShapeRepository.shapes.first())
    )
    val currentJigConfig: StateFlow<JigConfiguration> = _currentJigConfig.asStateFlow()

    // Active progressive step (1..10 = Config steps, 11 = Review, 12 = Final Product Result)
    private val _currentConfigStep = MutableStateFlow(1)
    val currentConfigStep: StateFlow<Int> = _currentConfigStep.asStateFlow()

    // Gemini AI Generation State
    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    private val _aiGenerationResult = MutableStateFlow<GeminiImageService.GenerationResult?>(null)
    val aiGenerationResult: StateFlow<GeminiImageService.GenerationResult?> = _aiGenerationResult.asStateFlow()

    private val _isAiResultViewBlueprint = MutableStateFlow(false)
    val isAiResultViewBlueprint: StateFlow<Boolean> = _isAiResultViewBlueprint.asStateFlow()

    // Active Jig or Rod Configuration
    private val _currentConfig = MutableStateFlow(
        ProductConfiguration(
            configType = ConfigType.JIG,
            productId = ProductCatalog.jigs.first().id,
            productName = ProductCatalog.jigs.first().name,
            modelNumber = ProductCatalog.jigs.first().modelNumber,
            category = ProductCatalog.jigs.first().category,
            imageUrl = ProductCatalog.jigs.first().imageUrl,
            weightGrams = ProductCatalog.jigs.first().defaultWeightGrams,
            lengthMm = ProductCatalog.jigs.first().defaultLengthMm,
            widthMm = ProductCatalog.jigs.first().defaultWidthMm,
            material = ProductCatalog.jigs.first().materials.first(),
            colorName = ProductCatalog.jigs.first().name.substringBefore(" Jigs"),
            baseColorHex = ProductCatalog.jigs.first().baseColorHex,
            accentColorHex = ProductCatalog.jigs.first().accentColorHex,
            patternType = ProductCatalog.jigs.first().patternType,
            frontRing = ProductCatalog.jigs.first().defaultFrontRing,
            backRing = ProductCatalog.jigs.first().defaultBackRing,
            hookTypeJig = ProductCatalog.jigs.first().defaultHook
        )
    )
    val currentConfig: StateFlow<ProductConfiguration> = _currentConfig.asStateFlow()

    // Current selected jig product definition (for ranges)
    private val _selectedJigProduct = MutableStateFlow<JigProduct>(ProductCatalog.jigs.first())
    val selectedJigProduct: StateFlow<JigProduct> = _selectedJigProduct.asStateFlow()

    // Current selected rod product definition (for ranges)
    private val _selectedRodProduct = MutableStateFlow<RodProduct>(ProductCatalog.rods.first())
    val selectedRodProduct: StateFlow<RodProduct> = _selectedRodProduct.asStateFlow()

    // Current selected lure product definition
    private val _selectedLureProduct = MutableStateFlow<LureProduct>(ProductCatalog.lures.first())
    val selectedLureProduct: StateFlow<LureProduct> = _selectedLureProduct.asStateFlow()

    // Saved Configurations Flow
    val savedConfigs: StateFlow<List<SavedConfigEntity>> = configDao.getAllConfigs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // PDF Generation State
    private val _pdfValidationResult = MutableStateFlow<PdfGenerator.PdfValidationResult?>(null)
    val pdfValidationResult: StateFlow<PdfGenerator.PdfValidationResult?> = _pdfValidationResult.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _saveStatusMessage = MutableStateFlow<String?>(null)
    val saveStatusMessage: StateFlow<String?> = _saveStatusMessage.asStateFlow()

    // -------------------------------------------------------------------------
    // PROGRESSIVE SHAPE-FIRST JIG CONFIGURATOR ACTIONS
    // -------------------------------------------------------------------------

    fun selectShape(shape: JigShapeTemplate) {
        val newConfig = JigConfiguration.fromShape(shape)
        _currentJigConfig.value = newConfig
        _currentConfigStep.value = 1
        _aiGenerationResult.value = null
        syncJigToProductConfig(newConfig)
    }

    fun setConfigStep(step: Int) {
        _currentConfigStep.value = step.coerceIn(1, 12)
    }

    fun nextConfigStep() {
        val next = (_currentConfigStep.value + 1).coerceAtMost(12)
        _currentConfigStep.value = next
    }

    fun previousConfigStep() {
        val prev = (_currentConfigStep.value - 1).coerceAtLeast(1)
        _currentConfigStep.value = prev
    }

    fun updateJigWeight(weight: Float, custom: String = "") {
        val customMap = if (custom.isNotBlank()) _currentJigConfig.value.customValues + ("weight" to custom) else _currentJigConfig.value.customValues
        val updated = _currentJigConfig.value.copy(weightGrams = weight, customValues = customMap)
        _currentJigConfig.value = updated
        syncJigToProductConfig(updated)
    }

    fun updateJigLength(length: Float, custom: String = "") {
        val customMap = if (custom.isNotBlank()) _currentJigConfig.value.customValues + ("length" to custom) else _currentJigConfig.value.customValues
        val updated = _currentJigConfig.value.copy(lengthMm = length, customValues = customMap)
        _currentJigConfig.value = updated
        syncJigToProductConfig(updated)
    }

    fun updateJigWidth(width: Float, custom: String = "") {
        val customMap = if (custom.isNotBlank()) _currentJigConfig.value.customValues + ("width" to custom) else _currentJigConfig.value.customValues
        val updated = _currentJigConfig.value.copy(widthMm = width, customValues = customMap)
        _currentJigConfig.value = updated
        syncJigToProductConfig(updated)
    }

    fun updateJigColors(mainColor: String, mainHex: Long, secondaryColor: String, secondaryHex: Long) {
        val updated = _currentJigConfig.value.copy(
            mainColor = mainColor,
            mainColorHex = mainHex,
            secondaryColor = secondaryColor,
            secondaryColorHex = secondaryHex
        )
        _currentJigConfig.value = updated
        syncJigToProductConfig(updated)
    }

    fun updateJigPattern(pattern: String, patternColor: String, patternColorHex: Long) {
        val updated = _currentJigConfig.value.copy(
            pattern = pattern,
            patternColor = patternColor,
            patternColorHex = patternColorHex
        )
        _currentJigConfig.value = updated
        syncJigToProductConfig(updated)
    }

    fun updateJigFinish(finish: String, custom: String = "") {
        val customMap = if (custom.isNotBlank()) _currentJigConfig.value.customValues + ("finish" to custom) else _currentJigConfig.value.customValues
        val updated = _currentJigConfig.value.copy(finish = finish, customValues = customMap)
        _currentJigConfig.value = updated
        syncJigToProductConfig(updated)
    }

    fun updateJigEye(eyeStyle: String, eyeColor: String) {
        val updated = _currentJigConfig.value.copy(eyeStyle = eyeStyle, eyeColor = eyeColor)
        _currentJigConfig.value = updated
        syncJigToProductConfig(updated)
    }

    fun updateJigAssistHook(hook: String, custom: String = "") {
        val customMap = if (custom.isNotBlank()) _currentJigConfig.value.customValues + ("assistHook" to custom) else _currentJigConfig.value.customValues
        val updated = _currentJigConfig.value.copy(assistHook = hook, customValues = customMap)
        _currentJigConfig.value = updated
        syncJigToProductConfig(updated)
    }

    fun updateJigAssistCord(cordColor: String, custom: String = "") {
        val customMap = if (custom.isNotBlank()) _currentJigConfig.value.customValues + ("assistCord" to custom) else _currentJigConfig.value.customValues
        val updated = _currentJigConfig.value.copy(assistCordColor = cordColor, customValues = customMap)
        _currentJigConfig.value = updated
        syncJigToProductConfig(updated)
    }

    fun updateJigRings(frontRing: String, backRing: String, customFront: String = "", customBack: String = "") {
        var customMap = _currentJigConfig.value.customValues
        if (customFront.isNotBlank()) customMap = customMap + ("frontRing" to customFront)
        if (customBack.isNotBlank()) customMap = customMap + ("backRing" to customBack)
        val updated = _currentJigConfig.value.copy(
            frontRing = frontRing,
            backRing = backRing,
            customValues = customMap
        )
        _currentJigConfig.value = updated
        syncJigToProductConfig(updated)
    }

    fun resetJigConfiguration() {
        val shape = JigShapeRepository.getById(_currentJigConfig.value.shapeId)
        val reset = JigConfiguration.fromShape(shape)
        _currentJigConfig.value = reset
        _currentConfigStep.value = 1
        _aiGenerationResult.value = null
        syncJigToProductConfig(reset)
    }

    fun toggleAiResultView() {
        _isAiResultViewBlueprint.value = !_isAiResultViewBlueprint.value
    }

    fun generateFinalAiProduct(
        referenceCanvasBitmap: Bitmap? = null,
        forceRegenerate: Boolean = false,
        customPrompt: String? = null
    ) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            val config = _currentJigConfig.value
            val result = geminiService.generateFinalProductImage(
                config = config,
                referenceCanvasBitmap = referenceCanvasBitmap,
                forceRegenerate = forceRegenerate,
                customPrompt = customPrompt
            )
            _aiGenerationResult.value = result
            _isGeneratingAi.value = false

            if (result is GeminiImageService.GenerationResult.Success) {
                // Update shared product config with the generated image file URI
                val imageUri = result.file.absolutePath
                syncJigToProductConfig(config, imageUrl = imageUri)
                _currentConfigStep.value = 12 // Advance to Final Product Result View
            }
        }
    }

    private fun syncJigToProductConfig(
        jigConfig: JigConfiguration = _currentJigConfig.value,
        imageUrl: String = _currentConfig.value.imageUrl
    ) {
        _currentConfig.value = jigConfig.toProductConfiguration(imageUrl)
    }

    fun selectJig(jig: JigProduct) {
        _selectedJigProduct.value = jig
        _currentConfig.value = ProductConfiguration(
            configType = ConfigType.JIG,
            productId = jig.id,
            productName = jig.name,
            modelNumber = jig.modelNumber,
            category = jig.category,
            imageUrl = jig.imageUrl,
            weightGrams = jig.defaultWeightGrams,
            lengthMm = jig.defaultLengthMm,
            widthMm = jig.defaultWidthMm,
            material = jig.materials.firstOrNull() ?: "Stainless Steel",
            colorName = jig.name.substringBefore(" Jigs"),
            baseColorHex = jig.baseColorHex,
            accentColorHex = jig.accentColorHex,
            patternType = jig.patternType,
            frontRing = jig.defaultFrontRing,
            backRing = jig.defaultBackRing,
            hookTypeJig = jig.defaultHook,
            threadColor = "None"
        )
    }

    fun selectLure(lure: LureProduct) {
        _selectedLureProduct.value = lure
        _currentConfig.value = ProductConfiguration(
            configType = ConfigType.LURE,
            productId = lure.id,
            productName = lure.name,
            modelNumber = lure.modelNumber,
            category = lure.category,
            weightGrams = lure.defaultWeightGrams,
            lengthMm = lure.defaultLengthMm,
            widthMm = lure.defaultWidthMm,
            material = lure.materials.firstOrNull() ?: "High-Impact ABS Resin",
            colorName = "Ghost Ayu",
            baseColorHex = lure.baseColorHex,
            accentColorHex = lure.accentColorHex,
            patternType = lure.patternType,
            divingDepthMeters = lure.divingDepthMeters,
            hookType = lure.hookType,
            buoyancy = lure.buoyancy
        )
    }

    fun selectRodCategory(category: RodCategoryType, lengthGroup: RodLengthGroup = RodLengthGroup.MEDIUM) {
        val rod = ProductCatalog.getRodByCategory(category) ?: ProductCatalog.rods.first()
        _selectedRodProduct.value = rod
        _currentConfig.value = ProductConfiguration(
            configType = ConfigType.ROD,
            productId = rod.id,
            productName = rod.name,
            modelNumber = rod.modelNumber,
            category = category.displayName,
            rodType = category.displayName,
            lengthMm = lengthGroup.defaultLengthMm,
            weightGrams = rod.defaultWeightGrams,
            widthMm = rod.defaultBlankDiameterMm,
            material = rod.materials.firstOrNull() ?: "30T Toray Carbon",
            power = rod.powers.firstOrNull() ?: "Medium",
            action = rod.actions.firstOrNull() ?: "Fast",
            sections = rod.sectionOptions.firstOrNull() ?: 2,
            maximumLoadKg = when (category) {
                RodCategoryType.SPINNING -> 10f
                RodCategoryType.CASTING -> 14f
                RodCategoryType.OFFSHORE_BOAT -> 25f
            },
            handleLengthMm = when (category) {
                RodCategoryType.SPINNING -> 400f
                RodCategoryType.CASTING -> 420f
                RodCategoryType.OFFSHORE_BOAT -> 480f
            },
            baseColorHex = 0xFF0F172A,
            accentColorHex = 0xFF0284C7
        )
    }

    fun updateWeight(weight: Float) {
        _currentConfig.value = _currentConfig.value.copy(weightGrams = weight)
    }

    fun updateLength(length: Float) {
        _currentConfig.value = _currentConfig.value.copy(lengthMm = length)
    }

    fun updateWidth(width: Float) {
        _currentConfig.value = _currentConfig.value.copy(widthMm = width)
    }

    fun updateMaterial(material: String) {
        _currentConfig.value = _currentConfig.value.copy(material = material)
    }

    fun updateColor(name: String, baseHex: Long, accentHex: Long) {
        _currentConfig.value = _currentConfig.value.copy(
            colorName = name,
            baseColorHex = baseHex,
            accentColorHex = accentHex
        )
    }

    fun updateThread(threadColor: String, colorHex: Long? = null) {
        _currentConfig.value = _currentConfig.value.copy(
            threadColor = threadColor,
            threadColorHex = colorHex
        )
    }

    fun updateFrontRing(ring: String) {
        _currentConfig.value = _currentConfig.value.copy(frontRing = ring)
    }

    fun updateBackRing(ring: String) {
        _currentConfig.value = _currentConfig.value.copy(backRing = ring)
    }

    fun updateHook(hook: String) {
        _currentConfig.value = _currentConfig.value.copy(hookTypeJig = hook)
    }

    fun updateFinish(finishName: String, baseHex: Long, accentHex: Long, patternType: JigPatternType) {
        _currentConfig.value = _currentConfig.value.copy(
            finishType = finishName,
            colorName = finishName,
            baseColorHex = baseHex,
            accentColorHex = accentHex,
            patternType = patternType
        )
    }

    fun updateCustomFrontRing(value: String) {
        _currentConfig.value = _currentConfig.value.copy(frontRing = "Custom", customFrontRing = value)
    }

    fun updateCustomBackRing(value: String) {
        _currentConfig.value = _currentConfig.value.copy(backRing = "Custom", customBackRing = value)
    }

    fun updateCustomHook(value: String) {
        _currentConfig.value = _currentConfig.value.copy(hookTypeJig = "Custom", customHook = value)
    }

    fun updateCustomAssistCord(value: String) {
        _currentConfig.value = _currentConfig.value.copy(threadColor = "Custom", customAssistCordColor = value)
    }

    fun updateCustomAssistCordColor(value: String) {
        _currentConfig.value = _currentConfig.value.copy(threadColor = "Custom", customAssistCordColor = value)
    }

    fun updateCustomFinish(value: String) {
        _currentConfig.value = _currentConfig.value.copy(finishType = "Custom", customFinish = value)
    }

    fun updateCustomWeight(value: String) {
        val floatVal = value.toFloatOrNull()
        _currentConfig.value = _currentConfig.value.copy(
            customWeight = value,
            weightGrams = floatVal ?: _currentConfig.value.weightGrams
        )
    }

    fun updateCustomWeight(weight: Float, customNote: String = "") {
        _currentConfig.value = _currentConfig.value.copy(weightGrams = weight, customWeight = customNote)
    }

    fun updateCustomLength(value: String) {
        val floatVal = value.toFloatOrNull()
        _currentConfig.value = _currentConfig.value.copy(
            customLength = value,
            lengthMm = floatVal ?: _currentConfig.value.lengthMm
        )
    }

    fun updateCustomLength(length: Float, customNote: String = "") {
        _currentConfig.value = _currentConfig.value.copy(lengthMm = length, customLength = customNote)
    }

    fun updateCustomWidth(value: String) {
        val floatVal = value.toFloatOrNull()
        _currentConfig.value = _currentConfig.value.copy(
            customWidth = value,
            widthMm = floatVal ?: _currentConfig.value.widthMm
        )
    }

    fun updateCustomWidth(width: Float, customNote: String = "") {
        _currentConfig.value = _currentConfig.value.copy(widthMm = width, customWidth = customNote)
    }

    /**
     * Updates shade / variant across finish, color, and selected product asset if a catalog product matches
     */
    fun selectProductShade(finishName: String, baseHex: Long, accentHex: Long, patternType: JigPatternType) {
        val matchingJig = ProductCatalog.jigs.firstOrNull { jig ->
            jig.name.contains(finishName, ignoreCase = true) ||
            (finishName.contains("Orange", ignoreCase = true) && finishName.contains("Black", ignoreCase = true) && jig.id == "jig_orange_black") ||
            (finishName.contains("Yellow", ignoreCase = true) && finishName.contains("Dot", ignoreCase = true) && jig.id == "jig_yellow_dotted") ||
            (finishName.contains("Yellow", ignoreCase = true) && finishName.contains("Orange", ignoreCase = true) && jig.id == "jig_yellow_orange") ||
            (finishName.contains("Blue", ignoreCase = true) && finishName.contains("Orange", ignoreCase = true) && jig.id == "jig_candy_blue_orange") ||
            (finishName.contains("Pink", ignoreCase = true) && finishName.contains("Green", ignoreCase = true) && jig.id == "jig_candy_pink_green") ||
            (finishName.contains("Yellow", ignoreCase = true) && finishName.contains("Black", ignoreCase = true) && jig.id == "jig_candy_yellow_black") ||
            (finishName.contains("Pink", ignoreCase = true) && finishName.contains("Blue", ignoreCase = true) && jig.id == "jig_crystal_pink_blue") ||
            (finishName.contains("Yellow", ignoreCase = true) && finishName.contains("Blue", ignoreCase = true) && jig.id == "jig_crystal_yellow_blue")
        }

        if (matchingJig != null) {
            _selectedJigProduct.value = matchingJig
            _currentConfig.value = _currentConfig.value.copy(
                productId = matchingJig.id,
                productName = matchingJig.name,
                modelNumber = matchingJig.modelNumber,
                imageUrl = matchingJig.imageUrl,
                colorName = finishName,
                baseColorHex = baseHex,
                accentColorHex = accentHex,
                patternType = patternType,
                finishType = finishName
            )
        } else {
            _currentConfig.value = _currentConfig.value.copy(
                colorName = finishName,
                baseColorHex = baseHex,
                accentColorHex = accentHex,
                patternType = patternType,
                finishType = finishName
            )
        }
    }

    fun updateEyeStyle(eyeStyle: String) {
        _currentConfig.value = _currentConfig.value.copy(eyeStyle = eyeStyle)
    }

    fun updateAssistCord(assistCord: String) {
        _currentConfig.value = _currentConfig.value.copy(assistCord = assistCord)
    }

    fun updateFinishType(finishType: String) {
        _currentConfig.value = _currentConfig.value.copy(finishType = finishType)
    }

    fun updateHookTypeJig(hook: String) {
        _currentConfig.value = _currentConfig.value.copy(hookTypeJig = hook)
    }

    fun updateRodParameters(
        power: String? = null,
        action: String? = null,
        sections: Int? = null,
        maxLoad: Float? = null,
        handleLength: Float? = null
    ) {
        val cur = _currentConfig.value
        _currentConfig.value = cur.copy(
            power = power ?: cur.power,
            action = action ?: cur.action,
            sections = sections ?: cur.sections,
            maximumLoadKg = maxLoad ?: cur.maximumLoadKg,
            handleLengthMm = handleLength ?: cur.handleLengthMm
        )
    }

    fun updateLureParameters(
        divingDepth: Float? = null,
        hookType: String? = null,
        buoyancy: String? = null
    ) {
        val cur = _currentConfig.value
        _currentConfig.value = cur.copy(
            divingDepthMeters = divingDepth ?: cur.divingDepthMeters,
            hookType = hookType ?: cur.hookType,
            buoyancy = buoyancy ?: cur.buoyancy
        )
    }

    fun generatePdf(context: Context) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            val config = _currentConfig.value
            val result = when (config.configType) {
                ConfigType.JIG -> PdfGenerator.generateJigPdf(context, config)
                ConfigType.ROD -> PdfGenerator.generateRodPdf(context, config)
                ConfigType.LURE -> PdfGenerator.generateLurePdf(context, config)
            }
            _pdfValidationResult.value = result
            _isGeneratingPdf.value = false
        }
    }

    fun saveCurrentConfig() {
        viewModelScope.launch {
            val c = _currentConfig.value
            val entity = SavedConfigEntity(
                configId = c.configId,
                referenceNumber = c.referenceNumber,
                configType = c.configType.name,
                productId = c.productId,
                productName = c.productName,
                modelNumber = c.modelNumber,
                category = c.category,
                weightGrams = c.weightGrams,
                lengthMm = c.lengthMm,
                widthMm = c.widthMm,
                material = c.material,
                colorName = c.colorName,
                baseColorHex = c.baseColorHex,
                accentColorHex = c.accentColorHex,
                rodType = c.rodType,
                power = c.power,
                action = c.action,
                sections = c.sections,
                recommendedLineWeight = c.recommendedLineWeight,
                recommendedLureWeight = c.recommendedLureWeight,
                maximumLoadKg = c.maximumLoadKg,
                handleLengthMm = c.handleLengthMm,
                divingDepthMeters = c.divingDepthMeters,
                hookType = c.hookType,
                buoyancy = c.buoyancy,
                frontRing = c.frontRing,
                backRing = c.backRing,
                hookTypeJig = c.hookTypeJig,
                threadColor = c.threadColor,
                threadWrapping = c.threadWrapping,
                finishType = c.finishType,
                imageUrl = c.imageUrl,
                notes = c.notes,
                timestamp = System.currentTimeMillis()
            )
            configDao.insertConfig(entity)
            _saveStatusMessage.value = "Configuration saved successfully!"
        }
    }

    fun loadSavedConfig(entity: SavedConfigEntity) {
        val configType = try { ConfigType.valueOf(entity.configType) } catch (e: Exception) { ConfigType.JIG }
        _currentConfig.value = ProductConfiguration(
            configId = entity.configId,
            referenceNumber = entity.referenceNumber,
            configType = configType,
            productId = entity.productId,
            productName = entity.productName,
            modelNumber = entity.modelNumber,
            category = entity.category,
            imageUrl = entity.imageUrl,
            weightGrams = entity.weightGrams,
            lengthMm = entity.lengthMm,
            widthMm = entity.widthMm,
            material = entity.material,
            colorName = entity.colorName,
            baseColorHex = entity.baseColorHex,
            accentColorHex = entity.accentColorHex,
            frontRing = entity.frontRing,
            backRing = entity.backRing,
            hookTypeJig = entity.hookTypeJig,
            threadColor = entity.threadColor,
            threadWrapping = entity.threadWrapping,
            finishType = entity.finishType,
            rodType = entity.rodType,
            power = entity.power,
            action = entity.action,
            sections = entity.sections,
            recommendedLineWeight = entity.recommendedLineWeight,
            recommendedLureWeight = entity.recommendedLureWeight,
            maximumLoadKg = entity.maximumLoadKg,
            handleLengthMm = entity.handleLengthMm,
            divingDepthMeters = entity.divingDepthMeters,
            hookType = entity.hookType,
            buoyancy = entity.buoyancy,
            notes = entity.notes,
            timestamp = entity.timestamp
        )
    }

    fun deleteSavedConfig(id: String) {
        viewModelScope.launch {
            configDao.deleteConfigById(id)
        }
    }

    fun clearStatusMessage() {
        _saveStatusMessage.value = null
    }
}
