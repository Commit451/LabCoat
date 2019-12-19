package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class RepositoryCommit(
    @Json(name = "id")
    var id: String,
    @Json(name = "short_id")
    var shortId: String? = null,
    @Json(name = "title")
    var title: String? = null,
    @Json(name = "author_name")
    var authorName: String? = null,
    @Json(name = "author_email")
    var authorEmail: String? = null,
    @Json(name = "created_at")
    var createdAt: Date? = null,
    @Json(name = "message")
    var message: String? = null
) : Parcelable
