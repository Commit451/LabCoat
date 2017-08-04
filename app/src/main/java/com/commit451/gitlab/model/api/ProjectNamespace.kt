package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel
class ProjectNamespace {
    @Json(name = "id")
    var id: Long = 0
    @Json(name = "name")
    var name: String? = null
    @Json(name = "path")
    var path: String? = null
    @Json(name = "owner_id")
    var ownerId: Long = 0
    @Json(name = "created_at")
    var createdAt: Date? = null
    @Json(name = "updated_at")
    var updatedAt: Date? = null
    @Json(name = "description")
    var description: String? = null
    @Json(name = "avatar")
    var avatar: Avatar? = null
    @Json(name = "public")
    var isPublic: Boolean = false

    @Parcel
    class Avatar {
        @Json(name = "url")
        var url: String? = null
    }
}
