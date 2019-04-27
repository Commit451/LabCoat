package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.Date

/**
 * A snippet!
 */
@Parcelize
data class Snippet(
    @field:Json(name = "id")
    var id: Long = 0,
    @field:Json(name = "title")
    var title: String? = null,
    @field:Json(name = "file_name")
    var fileName: String? = null,
    @field:Json(name = "author")
    var author: Author? = null,
    @field:Json(name = "expires_at")
    var expiresAt: Date? = null,
    @field:Json(name = "updated_at")
    var updatedAt: Date? = null,
    @field:Json(name = "created_at")
    var createdAt: Date? = null
) : Parcelable {
    @Parcelize
    class Author(
        @field:Json(name = "id")
        var id: Long = 0,
        @field:Json(name = "username")
        var username: String? = null,
        @field:Json(name = "email")
        var email: String? = null,
        @field:Json(name = "name")
        var name: String? = null,
        @field:Json(name = "state")
        var state: String? = null,
        @field:Json(name = "created_at")
        var createdAt: Date? = null
    ) : Parcelable
}
