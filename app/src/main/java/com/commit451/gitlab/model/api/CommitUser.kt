package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommitUser(
    @Json(name = "id")
    var id: String? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "username")
    var username: String? = null,
    @Json(name = "state")
    var state: String? = null,
    @Json(name = "avatar_url")
    var avatarUrl: String? = null,
    @Json(name = "web_url")
    var webUrl: String? = null
) : Parcelable
