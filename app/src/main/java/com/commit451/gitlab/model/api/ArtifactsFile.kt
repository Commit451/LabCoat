package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ArtifactsFile(
    @Json(name = "filename")
    var fileName: String = "",
    @Json(name = "size")
    var size: Long = 0
) : Parcelable
