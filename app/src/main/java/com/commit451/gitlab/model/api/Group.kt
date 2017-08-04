package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel

@Parcel
open class Group {
    @Json(name = "id")
    var id: Long = 0
    @Json(name = "name")
    var name: String? = null
    @Json(name = "path")
    var path: String? = null
    @Json(name = "description")
    var description: String? = null
    @Json(name = "avatar_url")
    var avatarUrl: String? = null
    @Json(name = "web_url")
    lateinit var webUrl: String
}
