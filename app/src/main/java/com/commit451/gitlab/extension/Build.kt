package com.commit451.gitlab.extension

import android.net.Uri
import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.model.api.Project

fun Build.getRawBuildUrl(baseUrl: Uri, project: Project): String {
    return baseUrl.toString() + project.pathWithNamespace + "/builds/" + id + "/raw"
}

fun Build.getDownloadBuildUrl(baseUrl: Uri, project: Project): String {
    return baseUrl.toString() + GitLab.API_VERSION + "/projects/" + project.id + "/builds/" + id + "/artifacts"
}
