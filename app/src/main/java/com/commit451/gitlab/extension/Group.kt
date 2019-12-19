package com.commit451.gitlab.extension

import com.commit451.gitlab.model.api.Group

val Group.feedUrl: String
    get() = "$webUrl.atom"
