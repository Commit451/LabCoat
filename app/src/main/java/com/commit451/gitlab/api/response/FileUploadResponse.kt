package com.commit451.gitlab.api.response

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

/**
 * Response when a file is uploaded
 */
@Parcelize
data class FileUploadResponse(
    @field:Json(name = "alt")
    var alt: String? = null,
    @field:Json(name = "url")
    var url: String? = null,
    @field:Json(name = "is_image")
    var isImage: Boolean = false,
    @field:Json(name = "markdown")
    var markdown: String
): Parcelable
