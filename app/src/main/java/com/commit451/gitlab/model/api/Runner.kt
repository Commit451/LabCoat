package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

/**
 * A runner. It runs builds. yeah
 */
@Parcelize
data class Runner(
    @Json(name = "id")
    var id: Long = 0,
    @Json(name = "description")
    var description: String? = null,
    @Json(name = "active")
    var isActive: Boolean = false,
    @Json(name = "is_shared")
    var isShared: Boolean = false,
    @Json(name = "name")
    var name: String? = null
) : Parcelable
