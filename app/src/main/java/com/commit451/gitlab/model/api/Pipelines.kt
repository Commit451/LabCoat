package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

/**
 * Represents a pipeline
 */
@Parcel
class Pipelines {

    @field:Json(name = "sha")
    var sha: String? = null
    @field:Json(name = "id")
    var id: Long = 0
    @field:Json(name = "ref")
    var ref: String? = null
    @field:Json(name = "status")
    var status: String? = null
}
