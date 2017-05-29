package com.commit451.gitlab.util


import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.support.v4.content.FileProvider
import com.commit451.okyo.Okyo
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
        val returnCursor = context.contentResolver.query(imageUri, null, null, null, null)
        var name = "file"

        if (returnCursor != null) {
            var nameIndex = returnCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            if (nameIndex == -1) {
                nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            }
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            if (!returnCursor.isClosed) {
                returnCursor.close()
            }
        }
        return name
    }

    fun uriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".easyphotopicker.fileprovider", file)
    }

    @Throws(IOException::class)
    fun saveBlobToProviderDirectory(context: Context, bytes: ByteArray, fileName: String): File {
        val targetFile = File(getProviderDirectory(context), fileName)
        targetFile.createNewFile()
        Okyo.writeByteArrayToFile(bytes, targetFile)
        return targetFile
    }

    /**
     * Piggy back off of EasyImage directory
     */
    fun getProviderDirectory(context: Context): File {
        var cacheDir = context.cacheDir

        if (isExternalStorageWritable()) {
            cacheDir = context.externalCacheDir
        }
        val dir = File(cacheDir, "EasyImage")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }
}
