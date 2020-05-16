package com.commit451.gitlab.util


import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

import com.commit451.gitlab.api.AuthenticationRequestInterceptor
import com.commit451.gitlab.model.Account

/**
 * Easy usage of [android.app.DownloadManager]
 */
object DownloadUtil {

    @Suppress("DEPRECATION")
    fun download(context: Context, account: Account, url: String, filename: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle(filename)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        request.addRequestHeader(AuthenticationRequestInterceptor.PRIVATE_TOKEN_HEADER_FIELD, account.privateToken)

        // get download service and enqueue file
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }
}
