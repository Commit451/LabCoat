package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Contributor(
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "email")
    var email: String? = null,
    @Json(name = "commits")
    var commits: Int = 0,
    @Json(name = "additions")
    var additions: Int = 0,
    @Json(name = "deletions")
    var deletions: Int = 0
): Parcelable
