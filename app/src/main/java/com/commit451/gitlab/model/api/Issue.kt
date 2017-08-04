package com.commit451.gitlab.model.api

import android.support.annotation.StringDef
import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel
class Issue {

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

    @Json(name = "id")
    var id: Long = 0
    @Json(name = "iid")
    var iid: Long = 0
    @Json(name = "project_id")
    var projectId: Long = 0
    @Json(name = "title")
    var title: String? = null
    @Json(name = "description")
    var description: String? = null
    @Json(name = "state")
    @State
    @get:State var state: String? = null
    @Json(name = "created_at")
    var createdAt: Date? = null
    @Json(name = "updated_at")
    var updatedAt: Date? = null
    @Json(name = "labels")
    var labels: List<String>? = null
    @Json(name = "milestone")
    var milestone: Milestone? = null
    @Json(name = "assignee")
    var assignee: UserBasic? = null
    @Json(name = "author")
    var author: UserBasic? = null
    @Json(name = "confidential")
    var isConfidential: Boolean = false
}
