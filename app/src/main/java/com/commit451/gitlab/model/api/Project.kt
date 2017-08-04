package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel
class Project {
    @field:Json(name = "id")
    var id: Long = 0
    @field:Json(name = "description")
    var description: String? = null
    @field:Json(name = "default_branch")
    var defaultBranch: String? = null
    @field:Json(name = "tag_list")
    var tagList: List<String>? = null
    @field:Json(name = "public")
    var isPublic: Boolean = false
    @field:Json(name = "archived")
    var isArchived: Boolean = false
    @field:Json(name = "visibility_level")
    var visibilityLevel: Int = 0
    @field:Json(name = "ssh_url_to_repo")
    var sshUrlToRepo: String? = null
    @field:Json(name = "http_url_to_repo")
    var httpUrlToRepo: String? = null
    @field:Json(name = "web_url")
    lateinit var webUrl: String
    @field:Json(name = "owner")
    var owner: UserBasic? = null
    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "name_with_namespace")
    var nameWithNamespace: String? = null
    @field:Json(name = "path")
    var path: String? = null
    @field:Json(name = "path_with_namespace")
    var pathWithNamespace: String? = null
    @field:Json(name = "issues_enabled")
    var isIssuesEnabled: Boolean? = null
    @field:Json(name = "merge_requests_enabled")
    var isMergeRequestsEnabled: Boolean? = null
    @field:Json(name = "wiki_enabled")
    var isWikiEnabled: Boolean? = null
    @field:Json(name = "builds_enabled")
    var isBuildEnabled: Boolean? = null
    @field:Json(name = "snippets_enabled")
    var isSnippetsEnabled: Boolean? = null
    @field:Json(name = "created_at")
    var createdAt: Date? = null
    @field:Json(name = "last_activity_at")
    var lastActivityAt: Date? = null
    @field:Json(name = "creator_id")
    var creatorId: Long = 0
    @field:Json(name = "namespace")
    lateinit var namespace: ProjectNamespace
    @field:Json(name = "forked_from_project")
    var forkedFromProject: ForkedFromProject? = null
    @field:Json(name = "avatar_url")
    var avatarUrl: String? = null
    @field:Json(name = "star_count")
    var starCount: Int = 0
    @field:Json(name = "forks_count")
    var forksCount: Int = 0
    @field:Json(name = "open_issues_count")
    var openIssuesCount: Int = 0
}
