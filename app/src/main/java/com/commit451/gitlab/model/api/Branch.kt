package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel

@Parcel(Parcel.Serialization.BEAN)
open class Branch {

    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "protected")
    var isProtected: Boolean = false
}
