package com.commit451.gitlab.util;


import com.commit451.gitlab.model.api.Build;
import com.commit451.gitlab.model.api.Project;

/**
 * Util related to {@link com.commit451.gitlab.model.api.Build} things
 */
public class BuildUtil {

    public static String getRawBuildUrl(String baseUrl, Project project, Build build) {
        return baseUrl + project.getPathWithNamespace() + "/builds/" + build.getId() + "/raw";
    }
}
