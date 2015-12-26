package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.net.Uri;

import java.util.Date;
import java.util.List;

@Parcel
public class Project {
    @SerializedName("id")
    long mId;
    @SerializedName("description")
    String mDescription;
    @SerializedName("default_branch")
    String mDefaultBranch;
    @SerializedName("tag_list")
    List<String> mTagList;
    @SerializedName("public")
    boolean mPublic;
    @SerializedName("archived")
    boolean mArchived;
    @SerializedName("visibility_level")
    int mVisibilityLevel;
    @SerializedName("ssh_url_to_repo")
    String mSshUrlToRepo;
    @SerializedName("http_url_to_repo")
    String mHttpUrlToRepo;
    @SerializedName("web_url")
    Uri mWebUrl;
    @SerializedName("owner")
    UserBasic mOwner;
    @SerializedName("name")
    String mName;
    @SerializedName("name_with_namespace")
    String mNameWithNamespace;
    @SerializedName("path")
    String mPath;
    @SerializedName("path_with_namespace")
    String mPathWithNamespace;
    @SerializedName("issues_enabled")
    boolean mIssuesEnabled;
    @SerializedName("merge_requests_enabled")
    boolean mMergeRequestsEnabled;
    @SerializedName("wiki_enabled")
    boolean mWikiEnabled;
    @SerializedName("build_enabled")
    boolean mBuildEnabled;
    @SerializedName("snippets_enabled")
    boolean mSnippetsEnabled;
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("last_activity_at")
    Date mLastActivityAt;
    @SerializedName("creator_id")
    long mCreatorId;
    @SerializedName("namespace")
    ProjectNamespace mNamespace;
    @SerializedName("forked_from_project")
    ForkedFromProject mForkedFromProject;
    @SerializedName("avatar_url")
    Uri mAvatarUrl;
    @SerializedName("star_count")
    int mStarCount;
    @SerializedName("forks_count")
    int mForksCount;
    @SerializedName("open_issues_count")
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

    public Uri getFeedUrl() {
        return Uri.parse(mWebUrl.toString() + ".atom");
    }
}
