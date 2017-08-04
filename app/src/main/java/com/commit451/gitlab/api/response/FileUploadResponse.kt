package com.commit451.gitlab.api.response

import com.squareup.moshi.Json

import org.parceler.Parcel

/**
 * Response when a file is uploaded
 */
@Parcel
class FileUploadResponse {

    @Json(name = "alt")
    var alt: String? = null
    @Json(name = "url")
    var url: String? = null
    @Json(name = "is_image")
    var isImage: Boolean = false
    @Json(name = "markdown")
    lateinit var markdown: String
}
