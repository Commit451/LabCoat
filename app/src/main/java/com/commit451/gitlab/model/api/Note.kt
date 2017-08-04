package com.commit451.gitlab.model.api

import android.support.annotation.StringDef
import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel
class Note {

    companion object {

        const val TYPE_ISSUE = "Issue"
        const val TYPE_MERGE_REQUEST = "MergeRequest"
    }

    @StringDef(TYPE_ISSUE, TYPE_MERGE_REQUEST)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type

    @Json(name = "id")
    var id: Long = 0
    @Json(name = "body")
    var body: String? = null
    @Json(name = "author")
    var author: UserBasic? = null
    @Json(name = "created_at")
    var createdAt: Date? = null
    @Json(name = "system")
    var isSystem: Boolean = false
    @Json(name = "noteable_id")
    var noteableId: Long = 0
    @Json(name = "noteable_type")
    @Type
    @get:Type var noteableType: String? = null
    @Json(name = "upvote?")
    var isUpvote: Boolean = false
    @Json(name = "downvote?")
    var isDownvote: Boolean = false
}
