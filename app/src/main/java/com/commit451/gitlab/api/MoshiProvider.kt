package com.commit451.gitlab.api

import com.commit451.gitlab.api.converter.DashDateAdapter
import com.squareup.moshi.Moshi

object MoshiProvider {

    val moshi: Moshi by lazy {
        Moshi.Builder()
                .add(DashDateAdapter())
                .build()
    }
}