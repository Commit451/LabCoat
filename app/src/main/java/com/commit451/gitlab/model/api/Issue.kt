package com.commit451.gitlab.model.api

import android.os.Parcelable
import androidx.annotation.StringDef
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class Issue(
    @field:Json(name = "id")
    var id: Long = 0,
    @field:Json(name = "iid")
    var iid: Long = 0,
    @field:Json(name = "project_id")
    var projectId: Long = 0,
    @field:Json(name = "title")
    var title: String? = null,
    @field:Json(name = "description")
    var description: String? = null,
    @field:Json(name = "state")
    @State
    @get:State
    var state: String? = null,
    @field:Json(name = "created_at")
    var createdAt: Date? = null,
    @field:Json(name = "updated_at")
    var updatedAt: Date? = null,
    @field:Json(name = "labels")
    var labels: List<String>? = null,
    @field:Json(name = "milestone")
    var milestone: Milestone? = null,
    @field:Json(name = "assignee")
    var assignee: User? = null,
    @field:Json(name = "author")
    var author: User? = null,
    @field:Json(name = "confidential")
    var isConfidential: Boolean = false
) : Parcelable {
    companion object {
        const val STATE_REOPEN = "reopen"
        const val STATE_CLOSE = "close"

        const val STATE_OPENED = "opened"
        const val STATE_REOPENED = "reopened"
        const val STATE_CLOSED = "closed"
    }

    @StringDef(STATE_REOPEN, STATE_CLOSE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class EditState

    @StringDef(STATE_OPENED, STATE_REOPENED, STATE_CLOSED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State
}
