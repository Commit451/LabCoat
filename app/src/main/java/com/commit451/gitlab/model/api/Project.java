package com.commit451.gitlab.model.api;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

@Parcel
@JsonObject
public class Project {
    @JsonField(name = "id")
    long id;
    @JsonField(name = "description")
    String description;
    @JsonField(name = "default_branch")
    String defaultBranch;
    @JsonField(name = "tag_list")
    List<String> tagList;
    @JsonField(name = "public")
    boolean isPublic;
    @JsonField(name = "archived")
    boolean archived;
    @JsonField(name = "visibility_level")
    int visibilityLevel;
    @JsonField(name = "ssh_url_to_repo")
    String sshUrlToRepo;
    @JsonField(name = "http_url_to_repo")
    String httpUrlToRepo;
    @JsonField(name = "web_url")
    Uri webUrl;
    @JsonField(name = "owner")
    UserBasic owner;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "name_with_namespace")
    String nameWithNamespace;
    @JsonField(name = "path")
    String path;
    @JsonField(name = "path_with_namespace")
    String pathWithNamespace;
    @JsonField(name = "issues_enabled")
    Boolean issuesEnabled;
    @JsonField(name = "merge_requests_enabled")
    Boolean mergeRequestsEnabled;
    @JsonField(name = "wiki_enabled")
    Boolean wikiEnabled;
    @JsonField(name = "builds_enabled")
    Boolean buildEnabled;
    @JsonField(name = "snippets_enabled")
    Boolean snippetsEnabled;
    @JsonField(name = "created_at")
    Date createdAt;
    @JsonField(name = "last_activity_at")
    Date lastActivityAt;
    @JsonField(name = "creator_id")
    long creatorId;
    @JsonField(name = "namespace")
    ProjectNamespace namespace;
    @JsonField(name = "forked_from_project")
    ForkedFromProject forkedFromProject;
    @JsonField(name = "avatar_url")
    Uri avatarUrl;
    @JsonField(name = "star_count")
    int starCount;
    @JsonField(name = "forks_count")
    int forksCount;
    @JsonField(name = "open_issues_count")
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

    public Uri getWebUrl() {
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

    public Uri getAvatarUrl() {
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
