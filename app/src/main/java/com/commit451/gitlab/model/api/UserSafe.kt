package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
open class UserSafe {
    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "username")
    var username: String? = null
}
