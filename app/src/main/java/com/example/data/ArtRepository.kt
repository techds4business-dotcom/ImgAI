package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.ImageConfig
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class ArtRepository(private val context: Context, private val database: AppDatabase) {
    private val dao = database.generatedImageDao()
    private val apiKey = BuildConfig.GEMINI_API_KEY

    val allImages: Flow<List<GeneratedImage>> = dao.getAllImages()

    suspend fun generateImage(
        prompt: String,
        selectedPreset: String,
        presetPromptSuffix: String,
        aspectRatio: String,
        imageSize: String
    ): GeneratedImage? = withContext(Dispatchers.IO) {
        val finalPrompt = if (presetPromptSuffix.isNotEmpty()) {
            "$prompt, $presetPromptSuffix"
        } else {
            prompt
        }

        try {
            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = finalPrompt)
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    imageConfig = ImageConfig(
                        aspectRatio = aspectRatio,
                        imageSize = imageSize
                    ),
                    responseModalities = listOf("IMAGE")
                )
            )

            // Let's use the mandated high quality image generation and editing model
            val modelName = "gemini-3.1-flash-image-preview"
            val response = RetrofitClient.service.generateContent(modelName, apiKey, request)

            val part = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()
            val inlineData = part?.inlineData

            if (inlineData != null && inlineData.data.isNotEmpty()) {
                val base64Data = inlineData.data
                val mimeType = inlineData.mimeType
                
                // Decode base64 and save as file
                val extension = if (mimeType.contains("png")) "png" else "jpg"
                val filename = "gen_${UUID.randomUUID()}.$extension"
                val file = File(context.filesDir, filename)
                
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                FileOutputStream(file).use { out ->
                    out.write(decodedBytes)
                }

                val imageRecord = GeneratedImage(
                    prompt = prompt,
                    selectedPreset = selectedPreset,
                    aspectRatio = aspectRatio,
                    imageSize = imageSize,
                    localFilePath = file.absolutePath,
                    timestamp = System.currentTimeMillis()
                )

                val id = dao.insertImage(imageRecord)
                return@withContext imageRecord.copy(id = id.toInt())
            } else {
                Log.e("ArtRepository", "No inlineData found in response.")
                throw IOException("No valid output image returned from the AI model. Ensure prompt complies with safety guidelines.")
            }
        } catch (e: Exception) {
            Log.e("ArtRepository", "Error generating image", e)
            throw e
        }
    }

    suspend fun editImage(
        baseImageRecord: GeneratedImage,
        editPrompt: String,
        aspectRatio: String,
        imageSize: String
    ): GeneratedImage? = withContext(Dispatchers.IO) {
        try {
            val file = File(baseImageRecord.localFilePath)
            if (!file.exists()) {
                throw IOException("Base image file does not exist locally.")
            }

            val imageBytes = file.readBytes()
            val base64Data = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val baseMimeType = if (baseImageRecord.localFilePath.endsWith("png")) "image/png" else "image/jpeg"

            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = editPrompt),
                            Part(
                                inlineData = InlineData(
                                    mimeType = baseMimeType,
                                    data = base64Data
                                )
                            )
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    imageConfig = ImageConfig(
                        aspectRatio = aspectRatio,
                        imageSize = imageSize
                    ),
                    responseModalities = listOf("IMAGE")
                )
            )

            // Let's use the mandated high quality image generation and editing model
            val modelName = "gemini-3.1-flash-image-preview"
            val response = RetrofitClient.service.generateContent(modelName, apiKey, request)

            val part = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()
            val inlineData = part?.inlineData

            if (inlineData != null && inlineData.data.isNotEmpty()) {
                val newBase64Data = inlineData.data
                val newMimeType = inlineData.mimeType
                
                // Decode base64 and save as file
                val extension = if (newMimeType.contains("png")) "png" else "jpg"
                val filename = "gen_edit_${UUID.randomUUID()}.$extension"
                val newFile = File(context.filesDir, filename)
                
                val decodedBytes = Base64.decode(newBase64Data, Base64.DEFAULT)
                FileOutputStream(newFile).use { out ->
                    out.write(decodedBytes)
                }

                val imageRecord = GeneratedImage(
                    prompt = baseImageRecord.prompt,
                    selectedPreset = baseImageRecord.selectedPreset,
                    aspectRatio = aspectRatio,
                    imageSize = imageSize,
                    localFilePath = newFile.absolutePath,
                    timestamp = System.currentTimeMillis(),
                    isEdited = true,
                    parentId = baseImageRecord.id,
                    editPrompt = editPrompt
                )

                val id = dao.insertImage(imageRecord)
                return@withContext imageRecord.copy(id = id.toInt())
            } else {
                Log.e("ArtRepository", "No inlineData found in edit response.")
                throw IOException("No valid output representation returned. Ensure prompt complies with safety guidelines.")
            }
        } catch (e: Exception) {
            Log.e("ArtRepository", "Error editing image", e)
            throw e
        }
    }

    suspend fun deleteImage(imageRecord: GeneratedImage) = withContext(Dispatchers.IO) {
        try {
            val file = File(imageRecord.localFilePath)
            if (file.exists()) {
                file.delete()
            }
            dao.deleteImage(imageRecord)
        } catch (e: Exception) {
            Log.e("ArtRepository", "Error deleting image history", e)
        }
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        try {
            // Delete files
            context.filesDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("gen_")) {
                    file.delete()
                }
            }
            dao.clearAllHistory()
        } catch (e: Exception) {
            Log.e("ArtRepository", "Error clearing all history", e)
        }
    }
}
