package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Identity(
    @field:Json(name = "provider")
    var provider: String? = null,
    @field:Json(name = "extern_uid")
    var externUid: String? = null
) : Parcelable
