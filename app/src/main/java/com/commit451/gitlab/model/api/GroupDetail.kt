package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class GroupDetail : Group() {
    @field:Json(name = "projects")
    var projects: List<Project>? = null
}
