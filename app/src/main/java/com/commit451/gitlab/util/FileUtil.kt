package com.commit451.gitlab.util


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.FileActivity
import com.commit451.gitlab.extension.base64Decode
import com.commit451.gitlab.extension.with
import com.commit451.gitlab.model.api.RepositoryFile
import com.commit451.gitlab.rx.CustomSingleObserver
import com.commit451.okyo.Okyo
import com.google.android.material.snackbar.Snackbar
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
    fun saveBlobToProviderDirectory(outDir: File, bytes: ByteArray, fileName: String): File {
        val targetFile = File(outDir, fileName)

        if(!targetFile.parentFile.exists()){
            targetFile.parentFile.mkdirs()
        }

        targetFile.createNewFile()
        Okyo.writeByteArrayToFile(bytes, targetFile)
        return targetFile

    }

    @Throws(IOException::class)
    fun saveBlobToProviderDirectory(context: Context, bytes: ByteArray, fileName: String): File {
       return saveBlobToProviderDirectory(getProviderDirectory(context), bytes, fileName)
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

    fun open(context: Context, projectId : Long, path : String, ref : String) : LiveData<Boolean> {

        val data = MutableLiveData<Boolean>()
        val callback = MutableLiveData<DownloadResult>()
        callback.observeForever { result ->

            if(result != null){

                val f = FileUtil.saveBlobToProviderDirectory(context, result.data, result.file.fileName!!)
                openFile(context, f)
                data.postValue(true)

            } else {
                data.postValue(false)
            }

        }

        DownloadTask(projectId, path, ref, callback).execute()
        return data

    }

    private class DownloadTask(private val projectId : Long, private val path : String, private val ref : String, private val callback : MutableLiveData<DownloadResult> ) : AsyncTask<Any, Any, DownloadResult>() {

        override fun doInBackground(vararg params: Any?): DownloadResult {
            val repoFile = App.get().gitLab.getFile(projectId, path, ref).blockingGet()
            val blob = repoFile.content.base64Decode().blockingGet()
            return DownloadResult(repoFile, blob)
        }

        override fun onPostExecute(result: DownloadResult?) {
            callback.postValue(result)
        }

    }

    private data class DownloadResult(val file: RepositoryFile, val data : ByteArray)

    fun download(context: Context, projectId : Long, path : String, ref : String) : LiveData<RepositoryFile> {

        val data = MutableLiveData<RepositoryFile>()
        App.get().gitLab.getFile(projectId, path, ref)
                .subscribe(object : CustomSingleObserver<RepositoryFile>() {

                    override fun error(t: Throwable) {
                        Timber.e(t)
                        data.postValue(null)
                    }

                    override fun success(repositoryFile: RepositoryFile) {
                        data.postValue(repositoryFile)
                    }
                })

        return data

    }

    fun loadBlob(context: Context, file : RepositoryFile) : LiveData<ByteArray> {

        val data = MutableLiveData<ByteArray>()
        file.content.base64Decode().subscribe(object : CustomSingleObserver<ByteArray>() {

                    override fun error(t: Throwable) {
                        data.postValue(null)
                    }

                    override fun success(bytes: ByteArray) {
                        data.postValue(bytes)
                    }
                })

        return data

    }

    fun openFile(context: Context, fileName: String, blob : ByteArray) : Boolean {


        val file = FileUtil.saveBlobToProviderDirectory(context, blob, fileName)
        return openFile(context, file)

    }

    fun openFile(context: Context, file: File) : Boolean {

        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val extension = FileActivity.fileExtension(file.absolutePath)
        if (extension.isNotEmpty()) {
            intent.type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.data = FileUtil.uriForFile(context, file)

        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Timber.e(e)
            false
        }

    }


}
