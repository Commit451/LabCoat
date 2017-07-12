package com.commit451.gitlab.extension

import android.net.Uri
import com.commit451.gitlab.model.api.Issue
import com.commit451.gitlab.model.api.Project

fun Issue.getUrl(project: Project): Uri {
    val projectUri = Uri.parse(project.webUrl)
    return projectUri.buildUpon()
            .appendPath("issues")
            .appendPath(iid.toString())
            .build()
}
