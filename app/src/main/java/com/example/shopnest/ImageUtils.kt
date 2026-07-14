package com.example.shopnest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {

    fun saveImage(context: Context, bitmap: Bitmap, fileName: String): String {
        val directory = context.filesDir
        val file = File(directory, fileName)

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }

    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun createUniqueFileName(prefix: String = "img"): String {
        return "${prefix}_${System.currentTimeMillis()}.jpg"
    }

    fun deleteImageFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}