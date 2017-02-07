package com.commit451.gitlab.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri

import com.commit451.gitlab.R

/**
 * Generates deeplinks
 */
object DeepLinker {

    val EXTRA_ORIGINAL_URI = "original_uri"

    fun generateDeeplinkIntentFromUri(context: Context, originalUri: Uri): Intent {
        val uri = originalUri.buildUpon()
                .scheme(context.getString(R.string.deeplink_scheme))
                .build()
        return generatePrivateIntent(context, uri, originalUri)
    }

    private fun generatePrivateIntent(context: Context, uri: Uri, originalUri: Uri): Intent {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.putExtra(EXTRA_ORIGINAL_URI, originalUri)
        intent.`package` = context.packageName
        return intent
    }
}
