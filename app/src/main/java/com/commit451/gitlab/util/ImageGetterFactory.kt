package com.commit451.gitlab.util

import android.text.Html
import android.widget.TextView
import com.commit451.gitlab.model.api.Project
import com.squareup.picasso.Picasso

object ImageGetterFactory {

    fun create(textView: TextView, picasso: Picasso, baseUrl: String, project: Project?): Html.ImageGetter {
        val getter = PicassoImageGetter(textView, picasso)
        getter.setSourceModifier { source ->
            if (source.startsWith("/")) {
                val url = baseUrl + "/" + project?.pathWithNamespace + source
                return@setSourceModifier url
            }
            source
        }
        return getter
    }
}