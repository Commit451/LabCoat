package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

/**
 * A runner. It runs builds. yeah
 */
@Parcelize
data class Runner(
    @field:Json(name = "id")
    var id: Long = 0,
    @field:Json(name = "description")
    var description: String? = null,
    @field:Json(name = "active")
    var isActive: Boolean = false,
    @field:Json(name = "is_shared")
    var isShared: Boolean = false,
    @field:Json(name = "name")
    var name: String? = null
) : Parcelable
