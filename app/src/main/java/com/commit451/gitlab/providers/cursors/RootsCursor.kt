package com.commit451.gitlab.providers.cursors

import android.annotation.TargetApi
import android.content.Context
import android.database.MatrixCursor
import android.os.Build
import android.provider.DocumentsContract
import com.commit451.gitlab.R
import com.commit451.gitlab.model.Account

@TargetApi(Build.VERSION_CODES.KITKAT)
class RootsCursor(projection : Array<String>? = arrayOf(DocumentsContract.Root.COLUMN_ROOT_ID, DocumentsContract.Root.COLUMN_MIME_TYPES, DocumentsContract.Root.COLUMN_FLAGS, DocumentsContract.Root.COLUMN_ICON, DocumentsContract.Root.COLUMN_TITLE, DocumentsContract.Root.COLUMN_SUMMARY, DocumentsContract.Root.COLUMN_DOCUMENT_ID, DocumentsContract.Root.COLUMN_AVAILABLE_BYTES)) : MatrixCursor(projection){

    fun addRoot(context: Context, rootId: String, documentId: String, account : Account){

        newRow().apply {

            add(DocumentsContract.Root.COLUMN_ROOT_ID, rootId)
            add(DocumentsContract.Root.COLUMN_SUMMARY, context.getString(R.string.fileprovider_account_summary, account.username, account.serverUrl))
            add(DocumentsContract.Root.COLUMN_TITLE, context.getString(R.string.app_name))
            add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, documentId)
            add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher)

        }

    }

}