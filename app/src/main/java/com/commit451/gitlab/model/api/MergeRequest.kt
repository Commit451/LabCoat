package com.commit451.gitlab.model.api

import android.os.Parcelable
import androidx.annotation.StringDef
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
open class MergeRequest(
    @Json(name = "id")
    var id: Long = 0,
    @Json(name = "iid")
    var iid: Long = 0,
    @Json(name = "project_id")
    var projectId: Long = 0,
    @Json(name = "title")
    var title: String? = null,
    @Json(name = "description")
    var description: String? = null,
    @Json(name = "state")
    @State
    @get:State
    var state: String? = null,
    @Json(name = "updated_at")
    var updatedAt: Date? = null,
    @Json(name = "created_at")
    var createdAt: Date? = null,
    @Json(name = "target_branch")
    var targetBranch: String? = null,
    @Json(name = "source_branch")
    var sourceBranch: String? = null,
    @Json(name = "upvotes")
    var upvotes: Long = 0,
    @Json(name = "downvotes")
    var downvotes: Long = 0,
    @Json(name = "author")
    var author: User? = null,
    @Json(name = "assignee")
    var assignee: User? = null,
    @Json(name = "source_project_id")
    var sourceProjectId: Long? = 0,
    @Json(name = "target_project_id")
    var targetProjectId: Long = 0,
    @Json(name = "labels")
    var labels: List<String>? = null,
    @Json(name = "work_in_progress")
    var isWorkInProgress: Boolean = false,
    @Json(name = "milestone")
    var milestone: Milestone? = null,
    @Json(name = "merge_when_build_succeeds")
    var isMergeWhenBuildSucceedsEnabled: Boolean = false,
    @Json(name = "merge_status")
    var mergeStatus: String? = null,

    /**
     * Get the changes. Only not null if this merge request was retrieved via [com.commit451.gitlab.api.GitLabService.getMergeRequestChanges]
     * @return the changes
     */
    @Json(name = "changes")
    var changes: MutableList<Diff>? = null
) : Parcelable {

    companion object {

        const val STATE_OPENED = "opened"
        const val STATE_MERGED = "merged"
        const val STATE_CLOSED = "closed"
    }

    @StringDef(STATE_OPENED, STATE_MERGED, STATE_CLOSED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State
}
