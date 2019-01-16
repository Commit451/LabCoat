package com.commit451.gitlab.model.api

import androidx.annotation.StringDef
import com.commit451.gitlab.api.converter.DashDateAdapter
import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel(Parcel.Serialization.BEAN)
open class Milestone {

    companion object {
        const val STATE_ACTIVE = "active"
        const val STATE_CLOSED = "closed"

        const val STATE_EVENT_ACTIVATE = "activate"
        const val STATE_EVENT_CLOSE = "close"
    }

    @StringDef(STATE_ACTIVE, STATE_CLOSED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State

    @StringDef(STATE_EVENT_ACTIVATE, STATE_EVENT_CLOSE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class StateEvent

    @field:Json(name = "id")
    var id: Long = 0
    @field:Json(name = "iid")
    var iid: Long = 0
    @field:Json(name = "project_id")
    var projectId: Long = 0
    @field:Json(name = "title")
    var title: String? = null
    @field:Json(name = "description")
    var description: String? = null
    @field:Json(name = "state")
    @get:State
    var state: String? = null
    @field:Json(name = "created_at")
    var createdAt: Date? = null
    @field:Json(name = "updated_at")
    var updatedAt: Date? = null
    @field:DashDateAdapter.DueDate
    @field:Json(name = "due_date")
    var dueDate: Date? = null
}
