package com.commit451.gitlab.util

import `in`.uncod.android.bypass.Bypass
import android.content.Context
import com.commit451.gitlab.activity.FullscreenImageActivity
import com.commit451.gitlab.model.api.Project

/**
 * Creates [BypassFactory]s which are configured to handle relative Urls
 */
object BypassFactory {

    fun create(context: Context): Bypass {
        return Bypass(context)
    }

    fun create(context: Context, project: Project): Bypass {
        val bypass = Bypass(context)
        bypass.setImageSpanClickListener { view, imageSpan, imageUrl ->
            val intent = FullscreenImageActivity.newIntent(view.context, project)
            intent.putExtra(FullscreenImageActivity.IMAGE_URL, imageUrl)
            context.startActivity(intent)
        }
        return bypass
    }
}