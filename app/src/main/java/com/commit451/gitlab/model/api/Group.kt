package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group (
    @field:Json(name = "id")
    var id: Long = 0,
    @field:Json(name = "name")
    var name: String? = null,
    @field:Json(name = "path")
    var path: String? = null,
    @field:Json(name = "description")
    var description: String? = null,
    @field:Json(name = "avatar_url")
    var avatarUrl: String? = null,
    @field:Json(name = "web_url")
    var webUrl: String,
    @field:Json(name = "projects")
    var projects: MutableList<Project>? = null
): Parcelable
