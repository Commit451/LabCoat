package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class Identity {
    @field:Json(name = "provider")
    var provider: String? = null
    @field:Json(name = "extern_uid")
    var externUid: String? = null
}
