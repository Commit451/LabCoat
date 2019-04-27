package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommitUser(
    @field:Json(name = "id")
    var id: String? = null,
    @field:Json(name = "name")
    var name: String? = null,
    @field:Json(name = "username")
    var username: String? = null,
    @field:Json(name = "state")
    var state: String? = null,
    @field:Json(name = "avatar_url")
    var avatarUrl: String? = null,
    @field:Json(name = "web_url")
    var webUrl: String? = null
) : Parcelable
