package com.commit451.gitlab.model.api

import android.support.annotation.StringDef
import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

/**
 * Todos. Not processing Target, since it is different depending on what the type is, which
 * makes it not play nice with any automated json parsing
 */
@Parcel
class Todo {

    companion object {

        const val TARGET_ISSUE = "Issue"
        const val TARGET_MERGE_REQUEST = "MergeRequest"

        const val STATE_PENDING = "pending"
        const val STATE_DONE = "done"
    }

    @StringDef(TARGET_ISSUE, TARGET_MERGE_REQUEST)
    @Retention(AnnotationRetention.SOURCE)
    annotation class TargetType

    @StringDef(STATE_PENDING, STATE_DONE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State

    @Json(name = "id")
    var id: String? = null
    @Json(name = "project")
    var project: Project? = null
    @Json(name = "author")
    var author: UserBasic? = null
    @Json(name = "action_name")
    var actionName: String? = null
    @Json(name = "target_type")
    @TargetType
    @get:TargetType
    var targetType: String? = null
    @Json(name = "target_url")
    var targetUrl: String? = null
    @Json(name = "body")
    var body: String? = null
    @Json(name = "state")
    @State
    @get:State
    var state: String? = null
    @Json(name = "created_at")
    var createdAt: Date? = null
}
