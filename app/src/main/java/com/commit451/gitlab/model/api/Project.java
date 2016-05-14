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
    long mId;
    @JsonField(name = "description")
    String mDescription;
    @JsonField(name = "default_branch")
    String mDefaultBranch;
    @JsonField(name = "tag_list")
    List<String> mTagList;
    @JsonField(name = "public")
    boolean mPublic;
    @JsonField(name = "archived")
    boolean mArchived;
    @JsonField(name = "visibility_level")
    int mVisibilityLevel;
    @JsonField(name = "ssh_url_to_repo")
    String mSshUrlToRepo;
    @JsonField(name = "http_url_to_repo")
    String mHttpUrlToRepo;
    @JsonField(name = "web_url")
    Uri mWebUrl;
    @JsonField(name = "owner")
    UserBasic mOwner;
    @JsonField(name = "name")
    String mName;
    @JsonField(name = "name_with_namespace")
    String mNameWithNamespace;
    @JsonField(name = "path")
    String mPath;
    @JsonField(name = "path_with_namespace")
    String mPathWithNamespace;
    @JsonField(name = "issues_enabled")
    boolean mIssuesEnabled;
    @JsonField(name = "merge_requests_enabled")
    boolean mMergeRequestsEnabled;
    @JsonField(name = "wiki_enabled")
    boolean mWikiEnabled;
    @JsonField(name = "builds_enabled")
    boolean mBuildEnabled;
    @JsonField(name = "snippets_enabled")
    boolean mSnippetsEnabled;
    @JsonField(name = "created_at")
    Date mCreatedAt;
    @JsonField(name = "last_activity_at")
    Date mLastActivityAt;
    @JsonField(name = "creator_id")
    long mCreatorId;
    @JsonField(name = "namespace")
    ProjectNamespace mNamespace;
    @JsonField(name = "forked_from_project")
    ForkedFromProject mForkedFromProject;
    @JsonField(name = "avatar_url")
    Uri mAvatarUrl;
    @JsonField(name = "star_count")
    int mStarCount;
    @JsonField(name = "forks_count")
    int mForksCount;
    @JsonField(name = "open_issues_count")
    int mOpenIssuesCount;

    public Project() {}

    public long getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getDefaultBranch() {
        return mDefaultBranch;
    }

    public List<String> getTagList() {
        return mTagList;
    }

    public boolean isPublic() {
        return mPublic;
    }

    public boolean isArchived() {
        return mArchived;
    }

    public int getVisibilityLevel() {
        return mVisibilityLevel;
    }

    public String getSshUrlToRepo() {
        return mSshUrlToRepo;
    }

    public String getHttpUrlToRepo() {
        return mHttpUrlToRepo;
    }

    public Uri getWebUrl() {
        return mWebUrl;
    }

    public UserBasic getOwner() {
        return mOwner;
    }

    public String getName() {
        return mName;
    }

    public String getNameWithNamespace() {
        return mNameWithNamespace;
    }

    public String getPath() {
        return mPath;
    }

    public String getPathWithNamespace() {
        return mPathWithNamespace;
    }

    public boolean isIssuesEnabled() {
        return mIssuesEnabled;
    }

    public boolean isMergeRequestsEnabled() {
        return mMergeRequestsEnabled;
    }

    public boolean isWikiEnabled() {
        return mWikiEnabled;
    }

    public boolean isBuildEnabled() {
        return mBuildEnabled;
    }

    public boolean isSnippetsEnabled() {
        return mSnippetsEnabled;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public Date getLastActivityAt() {
        return mLastActivityAt;
    }

    public long getCreatorId() {
        return mCreatorId;
    }

    public ProjectNamespace getNamespace() {
        return mNamespace;
    }

    public ForkedFromProject getForkedFromProject() {
        return mForkedFromProject;
    }

    public Uri getAvatarUrl() {
        return mAvatarUrl;
    }

    public int getStarCount() {
        return mStarCount;
    }

    public int getForksCount() {
        return mForksCount;
    }

    public int getOpenIssuesCount() {
        return mOpenIssuesCount;
    }

    @Nullable
    public Uri getFeedUrl() {
        if (mWebUrl == null) {
            return null;
        }
        return Uri.parse(mWebUrl + ".atom");
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
        return mId == project.mId;

    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }
}
