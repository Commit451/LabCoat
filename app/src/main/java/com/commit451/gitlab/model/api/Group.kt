package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group (
    @Json(name = "id")
    var id: Long = 0,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "path")
    var path: String? = null,
    @Json(name = "description")
    var description: String? = null,
    @Json(name = "visibility")
    var visibility: String? = null,
    @Json(name = "avatar_url")
    var avatarUrl: String? = null,
    @Json(name = "web_url")
    var webUrl: String? = null,
    @Json(name = "projects")
    var projects: List<Project>? = null
): Parcelable
