package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class Email {
    @field:Json(name = "id")
    var id: Long = 0
    @field:Json(name = "email")
    var email: String? = null
}
