package com.commit451.gitlab.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.material.snackbar.Snackbar
import android.view.View
import android.widget.Toast
import com.commit451.addendum.themeAttrColor
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.BaseActivity
import com.commit451.gitlab.extension.resolveUrl
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.navigation.BrowserFallback
import com.commit451.gitlab.navigation.LabCoatIntentCustomizer
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs

/**
 * All the things to do with intents
 */
object IntentUtil {

    fun openPage(activity: BaseActivity, url: String, account: Account? = null) {
        if (!activity.hasBrowsableLinks()) {
            throw IllegalStateException("You need to override hasBrowsableLinks and return true!")
        }
        val resolvedUrl = if (account == null) url else url.resolveUrl(account)

        val primaryColor = activity.themeAttrColor(R.attr.colorPrimary)
        SimpleChromeCustomTabs.getInstance()
                .withFallback(BrowserFallback(activity))
                .withIntentCustomizer(LabCoatIntentCustomizer(activity, primaryColor))
                .navigateTo(Uri.parse(resolvedUrl), activity)
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

    /**
     * Opens the link in the browser. Will never open in the app, even if it matches the schema
     */
    fun openBrowser(context: Context, url: String) {

        val intent = Intent(Intent.ACTION_VIEW)
        val uri = Uri.parse(url)
        intent.data = uri
        val resInfos = context.packageManager.queryIntentActivities(intent, 0)
        val intents = mutableListOf<Intent>()
        resInfos.forEach {
            if (!it.activityInfo.packageName.contains(context.packageName)) {
                val intentToAdd = Intent(Intent.ACTION_VIEW)
                intentToAdd.data = uri
                intentToAdd.setPackage(it.activityInfo.packageName)
                intents.add(intentToAdd)
            }
        }

        when {
            intents.isEmpty() -> {
                Toast.makeText(context, R.string.error_no_browser, Toast.LENGTH_SHORT)
                        .show()
            }
            intents.size == 1 -> try {
                context.startActivity(intents.first())
            } catch (e: Exception) {
                Toast.makeText(context, R.string.error_no_browser, Toast.LENGTH_SHORT)
                        .show()
            }
            else -> {
                // remove the first intent and show it as the main option
                val chooserIntent = Intent.createChooser(intents.removeAt(0), "Choose browser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray())
                context.startActivity(chooserIntent)
            }
        }
    }
}
