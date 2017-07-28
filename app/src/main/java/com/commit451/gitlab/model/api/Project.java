package com.commit451.gitlab.model.api;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

@Parcel
public class Project {
    @Json(name = "id")
    long id;
    @Json(name = "description")
    String description;
    @Json(name = "default_branch")
    String defaultBranch;
    @Json(name = "tag_list")
    List<String> tagList;
    @Json(name = "public")
    boolean isPublic;
    @Json(name = "archived")
    boolean archived;
    @Json(name = "visibility_level")
    int visibilityLevel;
    @Json(name = "ssh_url_to_repo")
    String sshUrlToRepo;
    @Json(name = "http_url_to_repo")
    String httpUrlToRepo;
    @Json(name = "web_url")
    String webUrl;
    @Json(name = "owner")
    UserBasic owner;
    @Json(name = "name")
    String name;
    @Json(name = "name_with_namespace")
    String nameWithNamespace;
    @Json(name = "path")
    String path;
    @Json(name = "path_with_namespace")
    String pathWithNamespace;
    @Json(name = "issues_enabled")
    Boolean issuesEnabled;
    @Json(name = "merge_requests_enabled")
    Boolean mergeRequestsEnabled;
    @Json(name = "wiki_enabled")
    Boolean wikiEnabled;
    @Json(name = "builds_enabled")
    Boolean buildEnabled;
    @Json(name = "snippets_enabled")
    Boolean snippetsEnabled;
    @Json(name = "created_at")
    Date createdAt;
    @Json(name = "last_activity_at")
    Date lastActivityAt;
    @Json(name = "creator_id")
    long creatorId;
    @Json(name = "namespace")
    ProjectNamespace namespace;
    @Json(name = "forked_from_project")
    ForkedFromProject forkedFromProject;
    @Json(name = "avatar_url")
    String avatarUrl;
    @Json(name = "star_count")
    int starCount;
    @Json(name = "forks_count")
    int forksCount;
    @Json(name = "open_issues_count")
    int openIssuesCount;

    public Project() {}

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isArchived() {
        return archived;
    }

    public int getVisibilityLevel() {
        return visibilityLevel;
    }

    public String getSshUrlToRepo() {
        return sshUrlToRepo;
    }

    public String getHttpUrlToRepo() {
        return httpUrlToRepo;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public UserBasic getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getNameWithNamespace() {
        return nameWithNamespace;
    }

    public String getPath() {
        return path;
    }

    public String getPathWithNamespace() {
        return pathWithNamespace;
    }

    public Boolean isIssuesEnabled() {
        return issuesEnabled;
    }

    public Boolean isMergeRequestsEnabled() {
        return mergeRequestsEnabled;
    }

    public Boolean isWikiEnabled() {
        return wikiEnabled;
    }

    public Boolean isBuildEnabled() {
        return buildEnabled;
    }

    public Boolean isSnippetsEnabled() {
        return snippetsEnabled;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getLastActivityAt() {
        return lastActivityAt;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public ProjectNamespace getNamespace() {
        return namespace;
    }

    public ForkedFromProject getForkedFromProject() {
        return forkedFromProject;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public int getStarCount() {
        return starCount;
    }

    public int getForksCount() {
        return forksCount;
    }

    public int getOpenIssuesCount() {
        return openIssuesCount;
    }

    @Nullable
    public Uri getFeedUrl() {
        if (webUrl == null) {
            return null;
        }
        return Uri.parse(webUrl + ".atom");
    }

    public boolean belongsToGroup() {
        //If there is an owner, then there is no group
        return getOwner() == null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Project)) {
            return false;
        }

        Project project = (Project) o;
        return id == project.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
