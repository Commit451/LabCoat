package com.commit451.gitlab.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

import com.commit451.gitlab.R
import com.novoda.simplechromecustomtabs.navigation.NavigationFallback

import java.lang.ref.WeakReference

/**
 * A fallback to open the url in the browser
 */
class BrowserFallback(context: Context) : NavigationFallback {

    private val context: WeakReference<Context>

    init {
        this.context = WeakReference(context)
    }

    override fun onFallbackNavigateTo(url: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = url
        val context = this.context.get() ?: return
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, R.string.error_no_browser, Toast.LENGTH_SHORT)
                    .show()
        }

    }
}