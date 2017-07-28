package com.commit451.gitlab.extension

import com.commit451.gitlab.model.api.Project
import android.net.Uri

/**
 * Created by johncarlson on 7/28/17.
 */
fun Project.getFeedUrl(): Uri? {
    if (webUrl == null) {
        return null
    }
    return Uri.parse(webUrl + ".atom")
}