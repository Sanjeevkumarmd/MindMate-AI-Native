package com.performex.mindmatenative.utils

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object PdfUtils {

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            PDFBoxResourceLoader.init(context)
            isInitialized = true
        }
    }

    suspend fun extractTextFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        init(context)
        var document: PDDocument? = null
        try {
            val contentResolver = context.contentResolver
            
            // Try to open stream directly
            val inputStream = try {
                contentResolver.openInputStream(uri)
            } catch (e: Exception) {
                // Fallback: Try to read from temp file if it exists and looks like it's from this URI
                val tempFile = File(context.cacheDir, "last_selected.pdf")
                if (tempFile.exists()) {
                    tempFile.inputStream()
                } else {
                    throw e
                }
            }

            if (inputStream != null) {
                inputStream.use { stream ->
                    document = PDDocument.load(stream)
                    val stripper = PDFTextStripper()
                    return@withContext stripper.getText(document).trim()
                }
            }
            return@withContext ""
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Failed to parse PDF: ${e.message}")
        } finally {
            document?.close()
        }
    }

    fun saveUriToTempFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "last_selected.pdf")
            tempFile.outputStream().use { outputStream ->
                inputStream.use { it.copyTo(outputStream) }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun getPdfBytes(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { 
                it.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
