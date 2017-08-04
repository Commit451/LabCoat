package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class ArtifactsFile {

    @Json(name = "filename")
    lateinit var fileName: String
    @Json(name = "size")
    var size: Long = 0
}
