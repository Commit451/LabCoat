package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

/**
 * Represents a pipeline
 */
@Parcel
class Pipelines {

    @Json(name = "sha")
    var sha: String? = null
    @Json(name = "id")
    var id: Long = 0
    @Json(name = "ref")
    var ref: String? = null
    @Json(name = "status")
    var status: String? = null
}
