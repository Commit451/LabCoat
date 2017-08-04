package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class Email {
    @Json(name = "id")
    var id: Long = 0
    @Json(name = "email")
    var email: String? = null
}
