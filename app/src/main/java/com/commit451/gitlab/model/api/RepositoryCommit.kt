package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel(Parcel.Serialization.BEAN)
open class RepositoryCommit {
    @field:Json(name = "id")
    lateinit var id: String
    @field:Json(name = "short_id")
    var shortId: String? = null
    @field:Json(name = "title")
    var title: String? = null
    @field:Json(name = "author_name")
    var authorName: String? = null
    @field:Json(name = "author_email")
    var authorEmail: String? = null
    @field:Json(name = "created_at")
    var createdAt: Date? = null
    @field:Json(name = "message")
    var message: String? = null
}
