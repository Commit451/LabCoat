package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class UserLogin : UserFull() {
    @field:Json(name = "private_token")
    var privateToken: String? = null
}
