package com.commit451.gitlab.extension

import com.commit451.gitlab.model.api.User

val User.feedUrl: String
    get() {
        return "$webUrl.atom"
    }
