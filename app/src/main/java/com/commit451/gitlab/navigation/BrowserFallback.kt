package com.commit451.gitlab.navigation

import android.content.Context
import android.net.Uri
import com.commit451.gitlab.util.IntentUtil
import com.novoda.simplechromecustomtabs.navigation.NavigationFallback
import java.lang.ref.WeakReference

/**
 * A fallback to open the url in the browser
 */
class BrowserFallback(context: Context) : NavigationFallback {

    private val context: WeakReference<Context> = WeakReference(context)

    override fun onFallbackNavigateTo(url: Uri) {
        val context = this.context.get() ?: return
        IntentUtil.openBrowser(context, url.toString())
    }
}