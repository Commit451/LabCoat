package com.commit451.gitlab.api.response

import com.squareup.moshi.Json

import org.parceler.Parcel

/**
 * Response when a file is uploaded
 */
@Parcel(Parcel.Serialization.BEAN)
open class FileUploadResponse {

    @field:Json(name = "alt")
    var alt: String? = null
    @field:Json(name = "url")
    var url: String? = null
    @field:Json(name = "is_image")
    var isImage: Boolean = false
    @field:Json(name = "markdown")
    lateinit var markdown: String
}
