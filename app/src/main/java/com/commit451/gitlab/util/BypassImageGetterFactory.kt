package com.commit451.gitlab.util

import android.widget.TextView

import com.commit451.bypasspicassoimagegetter.BypassPicassoImageGetter
import com.commit451.gitlab.model.api.Project
import com.squareup.picasso.Picasso

import timber.log.Timber

/**
 * Creates [BypassPicassoImageGetter]s which are configured to handle relative Urls
 */
object BypassImageGetterFactory {

    fun create(textView: TextView, picasso: Picasso, baseUrl: String, project: Project): BypassPicassoImageGetter {
        val getter = BypassPicassoImageGetter(textView, picasso)
        getter.setSourceModifier(BypassPicassoImageGetter.SourceModifier { source ->
            if (source.startsWith("/")) {
                val url = baseUrl + "/" + project.pathWithNamespace + source
                Timber.d(url)
                return@SourceModifier url
            }
            source
        })
        return getter
    }
}
