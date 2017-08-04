package com.commit451.gitlab.extension

import com.commit451.gitlab.model.api.UserBasic

val UserBasic.feedUrl: String get() {
    return webUrl + ".atom"
}