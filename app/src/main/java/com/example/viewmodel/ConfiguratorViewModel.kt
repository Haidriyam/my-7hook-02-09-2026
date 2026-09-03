package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SavedConfigEntity
import com.example.data.model.*
import com.example.data.pdf.PdfGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConfiguratorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val configDao = db.savedConfigDao()

    // Active Jig or Rod Configuration
    private val _currentConfig = MutableStateFlow(
        ProductConfiguration(
            configType = ConfigType.JIG,
            productId = ProductCatalog.jigs.first().id,
            productName = ProductCatalog.jigs.first().name,
            modelNumber = ProductCatalog.jigs.first().modelNumber,
            category = ProductCatalog.jigs.first().category,
            weightGrams = ProductCatalog.jigs.first().defaultWeightGrams,
            lengthMm = ProductCatalog.jigs.first().defaultLengthMm,
            widthMm = ProductCatalog.jigs.first().defaultWidthMm,
            material = ProductCatalog.jigs.first().materials.first(),
            colorName = ProductCatalog.jigs.first().name.substringBefore(" Jigs"),
            baseColorHex = ProductCatalog.jigs.first().baseColorHex,
            accentColorHex = ProductCatalog.jigs.first().accentColorHex,
            patternType = ProductCatalog.jigs.first().patternType
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

    fun selectJig(jig: JigProduct) {
        _selectedJigProduct.value = jig
        _currentConfig.value = ProductConfiguration(
            configType = ConfigType.JIG,
            productId = jig.id,
            productName = jig.name,
            modelNumber = jig.modelNumber,
            category = jig.category,
            weightGrams = jig.defaultWeightGrams,
            lengthMm = jig.defaultLengthMm,
            widthMm = jig.defaultWidthMm,
            material = jig.materials.firstOrNull() ?: "Stainless Steel",
            colorName = jig.name.substringBefore(" Jigs"),
            baseColorHex = jig.baseColorHex,
            accentColorHex = jig.accentColorHex,
            patternType = jig.patternType
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
            weightGrams = entity.weightGrams,
            lengthMm = entity.lengthMm,
            widthMm = entity.widthMm,
            material = entity.material,
            colorName = entity.colorName,
            baseColorHex = entity.baseColorHex,
            accentColorHex = entity.accentColorHex,
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
