package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedConfigDao {

    @Query("SELECT * FROM saved_configurations ORDER BY timestamp DESC")
    fun getAllConfigs(): Flow<List<SavedConfigEntity>>

    @Query("SELECT * FROM saved_configurations WHERE configId = :id")
    suspend fun getConfigById(id: String): SavedConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: SavedConfigEntity)

    @Query("DELETE FROM saved_configurations WHERE configId = :id")
    suspend fun deleteConfigById(id: String)

    @Query("SELECT * FROM saved_packaging ORDER BY timestamp DESC")
    fun getAllPackaging(): Flow<List<SavedPackagingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackaging(packaging: SavedPackagingEntity)

    @Query("DELETE FROM saved_packaging WHERE packagingId = :id")
    suspend fun deletePackagingById(id: String)
}
