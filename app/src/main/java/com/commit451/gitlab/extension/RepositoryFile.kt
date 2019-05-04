package com.commit451.gitlab.extension

import android.net.Uri
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryFile

fun RepositoryFile.getUrl(project: Project, branchName: String, currentPath: String): String {
    val projectUri = Uri.parse(project.webUrl)
    return projectUri.buildUpon()
            .appendPath("tree")
            .appendPath(branchName)
            .appendEncodedPath(currentPath)
            .build()
            .toString()
}
