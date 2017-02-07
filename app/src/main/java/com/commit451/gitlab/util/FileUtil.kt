package com.commit451.gitlab.util


import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

/**
 * Utility methods for uploading files
 */
object FileUtil {

    fun toPart(context: Context, imageUri: Uri): MultipartBody.Part? {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            val fileName = getFileName(context, imageUri)
            return toPart(bitmap, fileName)
        } catch (e: IOException) {
            //this won't happen, maybe
            Timber.e(e)
        }

        return null
    }

    fun toPart(file: File): MultipartBody.Part {
        val requestBody = RequestBody.create(MediaType.parse("image/png"), file)
        return MultipartBody.Part.createFormData("file", file.name, requestBody)
    }

    fun toPart(bitmap: Bitmap, name: String): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val requestBody = RequestBody.create(MediaType.parse("image/png"), stream.toByteArray())
        return MultipartBody.Part.createFormData("file", name, requestBody)
    }

    fun getFileName(context: Context, imageUri: Uri): String {
        val returnCursor = context.contentResolver.query(imageUri, null, null, null, null) ?: //This should probably just return null, but oh well
                return "file"

        var nameIndex = returnCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
        if (nameIndex == -1) {
            nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        }
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        if (!returnCursor.isClosed) {
            returnCursor.close()
        }
        return name
    }
}
