package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SavedPackagingEntity
import com.example.data.model.PackagingConfiguration
import com.example.data.pdf.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PackagingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val configDao = db.savedConfigDao()

    private val _packagingConfig = MutableStateFlow(PackagingConfiguration())
    val packagingConfig: StateFlow<PackagingConfiguration> = _packagingConfig.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _pdfValidationResult = MutableStateFlow<PdfGenerator.PdfValidationResult?>(null)
    val pdfValidationResult: StateFlow<PdfGenerator.PdfValidationResult?> = _pdfValidationResult.asStateFlow()

    private val _saveStatusMessage = MutableStateFlow<String?>(null)
    val saveStatusMessage: StateFlow<String?> = _saveStatusMessage.asStateFlow()

    fun updatePackagingType(type: String) {
        _packagingConfig.value = _packagingConfig.value.copy(packagingType = type)
    }

    fun updateCompanyName(name: String) {
        _packagingConfig.value = _packagingConfig.value.copy(companyName = name)
    }

    fun updateProductName(name: String) {
        _packagingConfig.value = _packagingConfig.value.copy(productName = name)
    }

    fun updateModelNumber(model: String) {
        _packagingConfig.value = _packagingConfig.value.copy(modelNumber = model)
    }

    fun updateCustomLogoUri(uri: String?) {
        _packagingConfig.value = _packagingConfig.value.copy(customLogoUri = uri)
    }

    fun updateDetails(
        description: String? = null,
        website: String? = null,
        phone: String? = null,
        email: String? = null,
        notes: String? = null,
        material: String? = null,
        dimensions: String? = null,
        cardStock: String? = null,
        windowStyle: String? = null,
        hangingSlot: String? = null,
        printingProcess: String? = null,
        quantity: String? = null
    ) {
        val cur = _packagingConfig.value
        _packagingConfig.value = cur.copy(
            productDescription = description ?: cur.productDescription,
            contactWebsite = website ?: cur.contactWebsite,
            contactPhone = phone ?: cur.contactPhone,
            contactEmail = email ?: cur.contactEmail,
            packagingNotes = notes ?: cur.packagingNotes,
            packagingMaterial = material ?: cur.packagingMaterial,
            packagingDimensions = dimensions ?: cur.packagingDimensions,
            cardStock = cardStock ?: cur.cardStock,
            windowStyle = windowStyle ?: cur.windowStyle,
            hangingSlot = hangingSlot ?: cur.hangingSlot,
            printingProcess = printingProcess ?: cur.printingProcess,
            targetQuantity = quantity ?: cur.targetQuantity
        )
    }

    fun generatePackagingPdf(context: Context) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            val result = PdfGenerator.generatePackagingPdf(context, _packagingConfig.value)
            _pdfValidationResult.value = result
            _isGeneratingPdf.value = false
        }
    }

    fun savePackagingConfig() {
        viewModelScope.launch {
            val c = _packagingConfig.value
            val entity = SavedPackagingEntity(
                packagingId = c.packagingId,
                referenceNumber = c.referenceNumber,
                packagingType = c.packagingType,
                productName = c.productName,
                modelNumber = c.modelNumber,
                companyName = c.companyName,
                customLogoUri = c.customLogoUri,
                productDescription = c.productDescription,
                contactWebsite = c.contactWebsite,
                contactPhone = c.contactPhone,
                contactEmail = c.contactEmail,
                packagingNotes = c.packagingNotes,
                packagingMaterial = c.packagingMaterial,
                packagingColor = c.packagingColor,
                packagingDimensions = c.packagingDimensions,
                timestamp = System.currentTimeMillis()
            )
            configDao.insertPackaging(entity)
            _saveStatusMessage.value = "Packaging configuration saved successfully!"
        }
    }

    fun clearStatusMessage() {
        _saveStatusMessage.value = null
    }
}
