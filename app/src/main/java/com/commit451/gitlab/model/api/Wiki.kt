package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Wiki(
    @field:Json(name = "content")
    var content: String? = null,
    @field:Json(name = "format")
    var format: String? = null,
    @field:Json(name = "slug")
    var slug: String? = null,
    @field:Json(name = "title")
    var title: String? = null
) : Parcelable
