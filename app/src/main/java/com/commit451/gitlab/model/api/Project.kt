package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel
class Project {
    @Json(name = "id")
    var id: Long = 0
    @Json(name = "description")
    var description: String? = null
    @Json(name = "default_branch")
    var defaultBranch: String? = null
    @Json(name = "tag_list")
    var tagList: List<String>? = null
    @Json(name = "public")
    var isPublic: Boolean = false
    @Json(name = "archived")
    var isArchived: Boolean = false
    @Json(name = "visibility_level")
    var visibilityLevel: Int = 0
    @Json(name = "ssh_url_to_repo")
    var sshUrlToRepo: String? = null
    @Json(name = "http_url_to_repo")
    var httpUrlToRepo: String? = null
    @Json(name = "web_url")
    lateinit var webUrl: String
    @Json(name = "owner")
    var owner: UserBasic? = null
    @Json(name = "name")
    var name: String? = null
    @Json(name = "name_with_namespace")
    var nameWithNamespace: String? = null
    @Json(name = "path")
    var path: String? = null
    @Json(name = "path_with_namespace")
    var pathWithNamespace: String? = null
    @Json(name = "issues_enabled")
    var isIssuesEnabled: Boolean? = null
    @Json(name = "merge_requests_enabled")
    var isMergeRequestsEnabled: Boolean? = null
    @Json(name = "wiki_enabled")
    var isWikiEnabled: Boolean? = null
    @Json(name = "builds_enabled")
    var isBuildEnabled: Boolean? = null
    @Json(name = "snippets_enabled")
    var isSnippetsEnabled: Boolean? = null
    @Json(name = "created_at")
    var createdAt: Date? = null
    @Json(name = "last_activity_at")
    var lastActivityAt: Date? = null
    @Json(name = "creator_id")
    var creatorId: Long = 0
    @Json(name = "namespace")
    lateinit var namespace: ProjectNamespace
    @Json(name = "forked_from_project")
    var forkedFromProject: ForkedFromProject? = null
    @Json(name = "avatar_url")
    var avatarUrl: String? = null
    @Json(name = "star_count")
    var starCount: Int = 0
    @Json(name = "forks_count")
    var forksCount: Int = 0
    @Json(name = "open_issues_count")
    var openIssuesCount: Int = 0
}
