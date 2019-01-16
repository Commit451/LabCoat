package com.commit451.gitlab.model.api

import androidx.annotation.StringDef
import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel(Parcel.Serialization.BEAN)
open class Note {

    companion object {

        const val TYPE_ISSUE = "Issue"
        const val TYPE_MERGE_REQUEST = "MergeRequest"
    }

    @StringDef(TYPE_ISSUE, TYPE_MERGE_REQUEST)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type

    @field:Json(name = "id")
    var id: Long = 0
    @field:Json(name = "body")
    var body: String? = null
    @field:Json(name = "author")
    var author: User? = null
    @field:Json(name = "created_at")
    var createdAt: Date? = null
    @field:Json(name = "system")
    var isSystem: Boolean = false
    @field:Json(name = "noteable_id")
    var noteableId: Long = 0
    @field:Json(name = "noteable_type")
    @Type
    @get:Type
    var noteableType: String? = null
    @field:Json(name = "upvote?")
    var isUpvote: Boolean = false
    @field:Json(name = "downvote?")
    var isDownvote: Boolean = false
}
