package com.commit451.gitlab.util;


import android.net.Uri;

import com.commit451.gitlab.api.GitLab;
import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;

/**
 * Util related to {@link com.commit451.gitlab.model.api.Build} things
 */
public class BuildUtil {

    public static String getRawBuildUrl(Uri baseUrl, Project project, Build build) {
        return baseUrl + project.getPathWithNamespace() + "/builds/" + build.getId() + "/raw";
    }

    public static String getDownloadBuildUrl(Uri baseUrl, Project project, Build build) {
        return baseUrl + GitLab.API_VERSION + "/projects/" + project.getId() + "/builds/"  + build.getId() + "/artifacts";
    }
}
