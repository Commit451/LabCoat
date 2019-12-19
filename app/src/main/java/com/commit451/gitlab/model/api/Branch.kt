package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Branch(
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "protected")
    var isProtected: Boolean = false
) : Parcelable
