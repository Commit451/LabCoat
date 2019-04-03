package com.commit451.gitlab.providers.cursors

import android.annotation.TargetApi
import android.database.MatrixCursor
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import com.commit451.gitlab.activity.FileActivity

@TargetApi(Build.VERSION_CODES.KITKAT)
class FilesCursor(projection : Array<String>? = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_FLAGS, DocumentsContract.Document.COLUMN_SIZE)) : MatrixCursor(projection) {

    private val mExtras = Bundle()

    fun addFile(documentId: String, name: String, size: Long? = 0, lastModified: Long? = 0) {

        newRow().apply {

            add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, documentId)
            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, name)
            add(DocumentsContract.Document.COLUMN_SIZE, size)
            add(DocumentsContract.Document.COLUMN_FLAGS, 0)
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, lastModified)
            add(DocumentsContract.Document.COLUMN_MIME_TYPE, MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileActivity.fileExtension(name)) ?: "application/octet-stream")

        }

    }

    fun addFolder(documentId: String, name: String, size: Long? = 0, lastModified: Long? = 0, prefix: String? = null){

        newRow().apply {

            add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, documentId)
            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, if(prefix == null) name else prefix + name)
            add(DocumentsContract.Document.COLUMN_SIZE, size ?: 0)
            add(DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.MIME_TYPE_DIR)
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, lastModified ?: 0)
            add(DocumentsContract.Document.COLUMN_FLAGS, 0)

        }

    }

    fun setHasMore(value : Boolean){
        mExtras.putBoolean(DocumentsContract.EXTRA_LOADING, value)
    }

    override fun getExtras(): Bundle {
        return mExtras
    }

}