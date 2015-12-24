package com.commit451.gitlab.model;

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
    @SerializedName("public")
    boolean mPublic;
    @SerializedName("visibility_level")
    int mVisibilityLevel;
    @SerializedName("ssh_url_to_repo")
    String mSshUrlToRepo;
    @SerializedName("http_url_to_repo")
    String mHttpUrlToRepo;
    @SerializedName("web_url")
    Uri mWebUrl;
    @SerializedName("tag_list")
    List<String> mTagList;
    @SerializedName("owner")
    User mOwner;
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
    @SerializedName("snippets_enabled")
    boolean mSnippetsEnabled;
    @SerializedName("build_enabled")
    boolean mBuildEnabled;
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("last_activity_at")
    Date mLastActivityAt;
    @SerializedName("creator_id")
    long mCreatorId;
    @SerializedName("namespace")
    Namespace mNamespace;
    @SerializedName("archived")
    boolean mArchived;
    @SerializedName("avatar_url")
    Uri mAvatarUrl;
    @SerializedName("star_count")
    int mStarCount;
    @SerializedName("forks_count")
    int mForksCount;

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

    public boolean isPublic() {
        return mPublic;
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

    public List<String> getTagList() {
        return mTagList;
    }

    public User getOwner() {
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

    public boolean isSnippetsEnabled() {
        return mSnippetsEnabled;
    }

    public boolean isBuildEnabled() {
        return mBuildEnabled;
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

    public Namespace getNamespace() {
        return mNamespace;
    }

    public boolean isArchived() {
        return mArchived;
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
}
