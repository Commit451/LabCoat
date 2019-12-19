package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class Project(
    @Json(name = "id")
    var id: Long = 0,
    @Json(name = "description")
    var description: String? = null,
    @Json(name = "default_branch")
    var defaultBranch: String? = null,
    @Json(name = "tag_list")
    var tagList: List<String>? = null,
    @Json(name = "archived")
    var isArchived: Boolean = false,
    @Json(name = "visibility")
    var visibility: String? = null,
    @Json(name = "ssh_url_to_repo")
    var sshUrlToRepo: String? = null,
    @Json(name = "http_url_to_repo")
    var httpUrlToRepo: String? = null,
    @Json(name = "web_url")
    var webUrl: String? = null,
    @Json(name = "owner")
    var owner: User? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "name_with_namespace")
    var nameWithNamespace: String? = null,
    @Json(name = "path")
    var path: String? = null,
    @Json(name = "path_with_namespace")
    var pathWithNamespace: String? = null,
    @Json(name = "issues_enabled")
    var isIssuesEnabled: Boolean? = null,
    @Json(name = "merge_requests_enabled")
    var isMergeRequestsEnabled: Boolean? = null,
    @Json(name = "wiki_enabled")
    var isWikiEnabled: Boolean? = null,
    @Json(name = "builds_enabled")
    var isBuildEnabled: Boolean? = null,
    @Json(name = "snippets_enabled")
    var isSnippetsEnabled: Boolean? = null,
    @Json(name = "created_at")
    var createdAt: Date? = null,
    @Json(name = "last_activity_at")
    var lastActivityAt: Date? = null,
    @Json(name = "creator_id")
    var creatorId: Long = 0,
    @Json(name = "namespace")
    var namespace: ProjectNamespace? = null,
    @Json(name = "forked_from_project")
    var forkedFromProject: ForkedFromProject? = null,
    @Json(name = "avatar_url")
    var avatarUrl: String? = null,
    @Json(name = "star_count")
    var starCount: Int = 0,
    @Json(name = "forks_count")
    var forksCount: Int = 0,
    @Json(name = "open_issues_count")
    var openIssuesCount: Int = 0
) : Parcelable {
    companion object {
        const val VISIBILITY_PRIVATE = "private"
        const val VISIBILITY_INTERNAL = "internal"
        const val VISIBILITY_PUBLIC = "public"
    }
}
