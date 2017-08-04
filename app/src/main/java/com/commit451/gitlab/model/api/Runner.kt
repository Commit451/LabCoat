package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

/**
 * A runner. It runs builds. yeah
 */
@Parcel
class Runner {

    @field:Json(name = "id")
    var id: Long = 0
    @field:Json(name = "description")
    var description: String? = null
    @field:Json(name = "active")
    var isActive: Boolean = false
    @field:Json(name = "is_shared")
    var isShared: Boolean = false
    @field:Json(name = "name")
    var name: String? = null
}
