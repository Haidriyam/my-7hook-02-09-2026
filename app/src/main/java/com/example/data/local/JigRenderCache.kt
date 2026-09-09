package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Deterministic Render Cache for 7Hooks AI-generated product images.
 * Guarantees that identical configurations retrieve the same saved image asset
 * without making redundant API calls.
 */
class JigRenderCache(context: Context) {

    private val cacheDir = File(context.filesDir, "jig_ai_renders").apply {
        if (!exists()) mkdirs()
    }

    // In-memory lookup map of renderKey -> File
    private val memoryIndex = mutableMapOf<String, File>()

    init {
        // Index existing files on startup
        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile && file.extension == "jpg") {
                val key = file.nameWithoutExtension.substringBefore("_rev")
                memoryIndex[key] = file
            }
        }
    }

    /**
     * Checks if a high-quality render already exists for this exact renderKey.
     */
    fun hasRender(renderKey: String): Boolean {
        val existing = memoryIndex[renderKey]
        return existing != null && existing.exists() && existing.length() > 0
    }

    /**
     * Retrieves the cached image file for this renderKey, or null if not yet rendered.
     */
    fun getRenderFile(renderKey: String): File? {
        val file = memoryIndex[renderKey] ?: File(cacheDir, "${renderKey}_rev1.jpg")
        return if (file.exists() && file.length() > 0) {
            memoryIndex[renderKey] = file
            file
        } else {
            null
        }
    }

    /**
     * Loads the cached Bitmap.
     */
    fun getRenderBitmap(renderKey: String): Bitmap? {
        val file = getRenderFile(renderKey) ?: return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Stores a freshly generated render in the deterministic cache.
     */
    @Synchronized
    fun putRender(renderKey: String, bitmap: Bitmap, revision: Int = 1): File {
        val targetFile = File(cacheDir, "${renderKey}_rev${revision}.jpg")
        FileOutputStream(targetFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            out.flush()
        }
        memoryIndex[renderKey] = targetFile
        return targetFile
    }

    /**
     * Stores raw bytes (e.g. from Base64 decoded response).
     */
    @Synchronized
    fun putRenderBytes(renderKey: String, bytes: ByteArray, revision: Int = 1): File {
        val targetFile = File(cacheDir, "${renderKey}_rev${revision}.jpg")
        FileOutputStream(targetFile).use { out ->
            out.write(bytes)
            out.flush()
        }
        memoryIndex[renderKey] = targetFile
        return targetFile
    }

    /**
     * Explicit regeneration helper: stores a new revision while preserving previous files.
     */
    @Synchronized
    fun putNewRevision(renderKey: String, bitmap: Bitmap): Pair<File, Int> {
        var rev = 1
        while (File(cacheDir, "${renderKey}_rev${rev}.jpg").exists()) {
            rev++
        }
        val targetFile = putRender(renderKey, bitmap, rev)
        return Pair(targetFile, rev)
    }

    fun clear() {
        cacheDir.listFiles()?.forEach { it.delete() }
        memoryIndex.clear()
    }
}
