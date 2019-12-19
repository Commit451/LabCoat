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
    @Json(name = "id")
    var id: Long = 0,
    @Json(name = "title")
    var title: String? = null,
    @Json(name = "file_name")
    var fileName: String? = null,
    @Json(name = "author")
    var author: Author? = null,
    @Json(name = "expires_at")
    var expiresAt: Date? = null,
    @Json(name = "updated_at")
    var updatedAt: Date? = null,
    @Json(name = "created_at")
    var createdAt: Date? = null
) : Parcelable {
    @Parcelize
    class Author(
        @Json(name = "id")
        var id: Long = 0,
        @Json(name = "username")
        var username: String? = null,
        @Json(name = "email")
        var email: String? = null,
        @Json(name = "name")
        var name: String? = null,
        @Json(name = "state")
        var state: String? = null,
        @Json(name = "created_at")
        var createdAt: Date? = null
    ) : Parcelable
}
