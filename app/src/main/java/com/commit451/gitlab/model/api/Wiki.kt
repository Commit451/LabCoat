package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel

@Parcel(Parcel.Serialization.BEAN)
open class Wiki {

    @field:Json(name = "content")
    var content: String? = null
    @field:Json(name = "format")
    var format: String? = null
    @field:Json(name = "slug")
    var slug: String? = null
    @field:Json(name = "title")
    var title: String? = null
}