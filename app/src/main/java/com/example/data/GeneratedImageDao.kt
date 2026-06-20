package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedImageDao {
    @Query("SELECT * FROM generated_images ORDER BY timestamp DESC")
    fun getAllImages(): Flow<List<GeneratedImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: GeneratedImage): Long

    @Query("SELECT * FROM generated_images WHERE id = :id LIMIT 1")
    suspend fun getImageById(id: Int): GeneratedImage?

    @Delete
    suspend fun deleteImage(image: GeneratedImage)

    @Query("DELETE FROM generated_images")
    suspend fun clearAllHistory()
}
