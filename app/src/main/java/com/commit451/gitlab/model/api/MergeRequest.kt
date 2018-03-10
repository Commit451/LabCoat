package com.commit451.gitlab.model.api

import android.support.annotation.StringDef
import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel(Parcel.Serialization.BEAN)
open class MergeRequest {

    companion object {

        const val STATE_OPENED = "opened"
        const val STATE_MERGED = "merged"
        const val STATE_CLOSED = "closed"
    }

    @StringDef(STATE_OPENED, STATE_MERGED, STATE_CLOSED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State

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
    @State
    @get:State
    var state: String? = null
    @field:Json(name = "updated_at")
    var updatedAt: Date? = null
    @field:Json(name = "created_at")
    var createdAt: Date? = null
    @field:Json(name = "target_branch")
    var targetBranch: String? = null
    @field:Json(name = "source_branch")
    var sourceBranch: String? = null
    @field:Json(name = "upvotes")
    var upvotes: Long = 0
    @field:Json(name = "downvotes")
    var downvotes: Long = 0
    @field:Json(name = "author")
    var author: User? = null
    @field:Json(name = "assignee")
    var assignee: User? = null
    @field:Json(name = "source_project_id")
    var sourceProjectId: Long = 0
    @field:Json(name = "target_project_id")
    var targetProjectId: Long = 0
    @field:Json(name = "labels")
    var labels: List<String>? = null
    @field:Json(name = "work_in_progress")
    var isWorkInProgress: Boolean = false
    @field:Json(name = "milestone")
    var milestone: Milestone? = null
    @field:Json(name = "merge_when_build_succeeds")
    var isMergeWhenBuildSucceedsEnabled: Boolean = false
    @field:Json(name = "merge_status")
    var mergeStatus: String? = null

    /**
     * Get the changes. Only not null if this merge request was retrieved via [com.commit451.gitlab.api.GitLabService.getMergeRequestChanges]
     * @return the changes
     */
    @field:Json(name = "changes")
    var changes: MutableList<Diff>? = null
}
