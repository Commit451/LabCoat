package com.commit451.gitlab.extension

import android.net.Uri
import android.support.annotation.DrawableRes
import com.commit451.gitlab.R
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryTreeObject

@DrawableRes
fun RepositoryTreeObject.getDrawableForType(): Int {
    if (type == null) {
        return R.drawable.ic_unknown_24dp
    }
    when (type) {
        RepositoryTreeObject.TYPE_FILE -> return R.drawable.ic_file_24dp
        RepositoryTreeObject.TYPE_FOLDER -> return R.drawable.ic_folder_24dp
        RepositoryTreeObject.TYPE_REPO -> return R.drawable.ic_repo_24dp
    }

    return R.drawable.ic_unknown_24dp
}

fun RepositoryTreeObject.getUrl(project: Project, branchName: String, currentPath: String): Uri {
    val projectUri = Uri.parse(project.webUrl)
    return projectUri.buildUpon()
            .appendPath("tree")
            .appendPath(branchName)
            .appendEncodedPath(currentPath)
            .appendPath(name)
            .build()
}