package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class ArtifactsFile {

    @field:Json(name = "filename")
    lateinit var fileName: String
    @field:Json(name = "size")
    var size: Long = 0
}
