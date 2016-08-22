package com.commit451.gitlab.util;


import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.commit451.gitlab.api.AuthenticationRequestInterceptor;
import com.commit451.gitlab.model.Account;

/**
 * Easy usage of {@link android.app.DownloadManager}
 */
public class DownloadUtil {

    public static void download(Context context, Account account, String url, String filename) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(filename);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        request.addRequestHeader(AuthenticationRequestInterceptor.PRIVATE_TOKEN_HEADER_FIELD, account.getPrivateToken());

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}
