package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel(Parcel.Serialization.BEAN)
open class ForkedFromProject {
    @field:Json(name = "id")
    var id: Long = 0
    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "name_with_namespace")
    var nameWithNamespace: String? = null
    @field:Json(name = "path")
    var path: String? = null
    @field:Json(name = "path_with_namespace")
    var pathWithNamespace: String? = null
}
