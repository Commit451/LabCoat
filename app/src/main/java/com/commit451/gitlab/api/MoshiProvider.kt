package com.commit451.gitlab.api

import com.commit451.gitlab.api.converter.DashDateAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

object MoshiProvider {

    val moshi: Moshi by lazy {
        Moshi.Builder()
                .add(DashDateAdapter())
                .add(KotlinJsonAdapterFactory())
                .add(Date::class.java, Rfc3339DateJsonAdapter())
                .build()
    }
}
