package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel

@Parcel(Parcel.Serialization.BEAN)
open class Group {
    @field:Json(name = "id")
    var id: Long = 0
    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "path")
    var path: String? = null
    @field:Json(name = "description")
    var description: String? = null
    @field:Json(name = "avatar_url")
    var avatarUrl: String? = null
    @field:Json(name = "web_url")
    lateinit var webUrl: String
    @field:Json(name = "projects")
    var projects: MutableList<Project>? = null
}
