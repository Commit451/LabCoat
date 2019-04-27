package com.commit451.gitlab.model.api

import android.os.Parcelable
import androidx.annotation.StringDef
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.Date

/**
 * Todos. Not processing Target, since it is different depending on what the type is, which
 * makes it not play nice with any automated json parsing
 */
@Parcelize
data class Todo(
    @field:Json(name = "id")
    var id: String? = null,
    @field:Json(name = "project")
    var project: Project? = null,
    @field:Json(name = "author")
    var author: User? = null,
    @field:Json(name = "action_name")
    var actionName: String? = null,
    @field:Json(name = "target_type")
    @TargetType
    @get:TargetType
    var targetType: String? = null,
    @field:Json(name = "target_url")
    var targetUrl: String? = null,
    @field:Json(name = "body")
    var body: String? = null,
    @field:Json(name = "state")
    @State
    @get:State
    var state: String? = null,
    @field:Json(name = "created_at")
    var createdAt: Date? = null
) : Parcelable {
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
}
