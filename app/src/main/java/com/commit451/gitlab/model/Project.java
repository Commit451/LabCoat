package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.net.Uri;

import java.util.Date;
import java.util.List;

/**
 * A Project. You know, like this one.
 */
@Parcel
public class Project {

    @SerializedName("id")
    long mId;
    @SerializedName("description")
    String mDescription;
    @SerializedName("default_branch")
    String mDefaultBranch;
    @SerializedName("public")
    Boolean mPublic;
    @SerializedName("visibility_level")
    Integer mVisibilityLevel;
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
    Boolean mIssuesEnabled;
    @SerializedName("merge_requests_enabled")
    Boolean mMergeRequestsEnabled;
    @SerializedName("wiki_enabled")
    Boolean mWikiEnabled;
    @SerializedName("snippets_enabled")
    Boolean mSnippetsEnabled;
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("last_activity_at")
    Date mLastActivityAt;
    @SerializedName("creator_id")
    Long mCreatorId;
    @SerializedName("namespace")
    Namespace mNamespace;
    @SerializedName("archived")
    Boolean mArchived;
    @SerializedName("avatar_url")
    Uri mAvatarUrl;
    @SerializedName("star_count")
    Integer mStarCount;
    @SerializedName("forks_count")
    Integer mForksCount;

    public Project(){}

    public long getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getDefaultBranch() {
        return mDefaultBranch;
    }

    public Boolean getPublic() {
        return mPublic;
    }

    public Integer getVisibilityLevel() {
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

    public Boolean getIssuesEnabled() {
        return mIssuesEnabled;
    }

    public Boolean getMergeRequestsEnabled() {
        return mMergeRequestsEnabled;
    }

    public Boolean getWikiEnabled() {
        return mWikiEnabled;
    }

    public Boolean getSnippetsEnabled() {
        return mSnippetsEnabled;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public Date getLastActivityAt() {
        return mLastActivityAt;
    }

    public Long getCreatorId() {
        return mCreatorId;
    }

    public Namespace getNamespace() {
        return mNamespace;
    }

    public Boolean getArchived() {
        return mArchived;
    }

    public Uri getAvatarUrl() {
        return mAvatarUrl;
    }

    public Integer getStarCount() {
        return mStarCount;
    }

    public Integer getForksCount() {
        return mForksCount;
    }
}
