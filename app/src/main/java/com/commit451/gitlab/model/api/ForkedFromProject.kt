package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class ForkedFromProject {
    @Json(name = "id")
    var id: Long = 0
    @Json(name = "name")
    var name: String? = null
    @Json(name = "name_with_namespace")
    var nameWithNamespace: String? = null
    @Json(name = "path")
    var path: String? = null
    @Json(name = "path_with_namespace")
    var pathWithNamespace: String? = null
}
