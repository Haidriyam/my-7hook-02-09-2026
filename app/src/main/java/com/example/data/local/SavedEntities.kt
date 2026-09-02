package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_configurations")
data class SavedConfigEntity(
    @PrimaryKey val configId: String,
    val referenceNumber: String,
    val configType: String, // "JIG" or "ROD"
    val productId: String,
    val productName: String,
    val modelNumber: String,
    val category: String,
    val weightGrams: Float,
    val lengthMm: Float,
    val widthMm: Float,
    val material: String,
    val colorName: String,
    val baseColorHex: Long,
    val accentColorHex: Long,
    val rodType: String,
    val power: String,
    val action: String,
    val sections: Int,
    val recommendedLineWeight: String,
    val recommendedLureWeight: String,
    val maximumLoadKg: Float,
    val handleLengthMm: Float,
    val notes: String,
    val timestamp: Long
)

@Entity(tableName = "saved_packaging")
data class SavedPackagingEntity(
    @PrimaryKey val packagingId: String,
    val referenceNumber: String,
    val packagingType: String,
    val productName: String,
    val modelNumber: String,
    val companyName: String,
    val customLogoUri: String?,
    val productDescription: String,
    val contactWebsite: String,
    val contactPhone: String,
    val contactEmail: String,
    val packagingNotes: String,
    val packagingMaterial: String,
    val packagingColor: String,
    val packagingDimensions: String,
    val timestamp: Long
)
