package com.commit451.gitlab.extension

import com.commit451.gitlab.api.GitLabService
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.model.api.Project

fun Build.getRawBuildUrl(baseUrl: String, project: Project): String {
    return baseUrl + project.pathWithNamespace + "/builds/" + id + "/raw"
}

fun Build.getDownloadBuildUrl(baseUrl: String, project: Project): String {
    return baseUrl + GitLabService.API_VERSION + "/projects/" + project.id + "/jobs/" + id + "/artifacts"
}
