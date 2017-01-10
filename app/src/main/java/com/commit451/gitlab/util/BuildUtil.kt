package com.commit451.gitlab.util


import android.net.Uri

import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.model.api.Build
import com.commit451.gitlab.model.api.Project

/**
 * Util related to [com.commit451.gitlab.model.api.Build] things
 */
object BuildUtil {

    fun getRawBuildUrl(baseUrl: Uri, project: Project, build: Build): String {
        return baseUrl.toString() + project.pathWithNamespace + "/builds/" + build.id + "/raw"
    }

    fun getDownloadBuildUrl(baseUrl: Uri, project: Project, build: Build): String {
        return baseUrl.toString() + GitLab.API_VERSION + "/projects/" + project.id + "/builds/" + build.id + "/artifacts"
    }
}
