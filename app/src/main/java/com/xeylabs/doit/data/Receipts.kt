package com.xeylabs.doit.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object Receipts {
    fun saveReceipt(context: Context, uri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "receipts").apply { mkdirs() }
            val ext = when (context.contentResolver.getType(uri)) {
                "image/png" -> ".png"
                "image/webp" -> ".webp"
                else -> ".jpg"
            }
            val name = UUID.randomUUID().toString() + ext
            val out = File(dir, name)
            context.contentResolver.openInputStream(uri)?.use { input ->
                out.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            "receipts/$name"
        } catch (e: Exception) {
            null
        }
    }

    fun deleteReceipt(context: Context, relativePath: String?) {
        if (relativePath.isNullOrBlank()) return
        try {
            val file = File(context.filesDir, relativePath)
            if (file.exists()) file.delete()
        } catch (e: Exception) {
        }
    }

    fun deleteAllReceipts(context: Context) {
        try {
            File(context.filesDir, "receipts").deleteRecursively()
        } catch (e: Exception) {
        }
    }

    fun receiptFile(context: Context, relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        val file = File(context.filesDir, relativePath)
        return if (file.exists()) file else null
    }
}
