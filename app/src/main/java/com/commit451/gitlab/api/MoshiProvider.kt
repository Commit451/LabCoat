package com.commit451.gitlab.api

import com.commit451.gitlab.api.converter.DashDateAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import java.util.*

object MoshiProvider {

    val moshi: Moshi by lazy {
        Moshi.Builder()
                .add(DashDateAdapter())
                .add(Date::class.java, Rfc3339DateJsonAdapter())
                .build()
    }
}