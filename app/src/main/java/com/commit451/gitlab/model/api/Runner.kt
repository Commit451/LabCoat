package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

/**
 * A runner. It runs builds. yeah
 */
@Parcel
class Runner {

    @Json(name = "id")
    var id: Long = 0
    @Json(name = "description")
    var description: String? = null
    @Json(name = "active")
    var isActive: Boolean = false
    @Json(name = "is_shared")
    var isShared: Boolean = false
    @Json(name = "name")
    var name: String? = null
}
