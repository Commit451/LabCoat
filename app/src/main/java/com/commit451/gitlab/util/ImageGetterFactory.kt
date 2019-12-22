package com.commit451.gitlab.util

import android.text.Html
import android.widget.TextView
import com.commit451.gitlab.model.api.Project

object ImageGetterFactory {

    fun create(textView: TextView, baseUrl: String, project: Project?): Html.ImageGetter {
        return CoilImageGetter(textView) { source ->
            if (source.startsWith("/")) {
                baseUrl + "/" + project?.pathWithNamespace + source
            } else {
                source
            }
        }
    }
}
