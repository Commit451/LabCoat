package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class Contributor {

    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "email")
    var email: String? = null
    @field:Json(name = "commits")
    var commits: Int = 0
    @field:Json(name = "additions")
    var additions: Int = 0
    @field:Json(name = "deletions")
    var deletions: Int = 0
}
