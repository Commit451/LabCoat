package com.commit451.gitlab.extension

import com.commit451.gitlab.model.api.Project

val Project.feedUrl: String get() = webUrl + ".atom"

fun Project.belongsToGroup(): Boolean {
    //If there is an owner, then there is no group
    return owner == null
}