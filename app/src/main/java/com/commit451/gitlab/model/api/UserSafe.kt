package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
open class UserSafe {
    @Json(name = "name")
    var name: String? = null
    @Json(name = "username")
    var username: String? = null
}
