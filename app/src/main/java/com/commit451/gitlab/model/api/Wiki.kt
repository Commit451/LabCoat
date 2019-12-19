package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Wiki(
    @Json(name = "content")
    var content: String? = null,
    @Json(name = "format")
    var format: String? = null,
    @Json(name = "slug")
    var slug: String? = null,
    @Json(name = "title")
    var title: String? = null
) : Parcelable
