package com.commit451.gitlab.util

import android.content.Intent
import android.net.Uri
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.view.View
import com.commit451.easel.Easel
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.navigation.BrowserFallback
import com.commit451.gitlab.navigation.LabCoatIntentCustomizer
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs

/**
 * All the things to do with intents
 */
object IntentUtil {

    fun openPage(activity: BaseActivity, url: String) {
        if (!activity.hasBrowsableLinks()) {
            throw IllegalStateException("You need to override hasBrowsableLinks and return true!")
        }
        if (TextUtils.isEmpty(url)) {
            return
        }

        val primaryColor = Easel.getThemeAttrColor(activity, R.attr.colorPrimary)
        SimpleChromeCustomTabs.getInstance()
                .withFallback(BrowserFallback(activity))
                .withIntentCustomizer(LabCoatIntentCustomizer(activity, primaryColor))
                .navigateTo(Uri.parse(url), activity)
    }

    fun share(root: View, url: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, url.toString())
        try {
            root.context.startActivity(shareIntent)
        } catch (e: Exception) {
            Snackbar.make(root, R.string.error_could_not_share, Snackbar.LENGTH_SHORT)
                    .show()
        }

    }
}
