package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

/**
 * Merge that request!
 * Created by Jawn on 9/20/2015.
 */
@Parcel
public class MergeRequest {
    @SerializedName("id")
    long mId;
    @SerializedName("iid")
    long mIid;
    @SerializedName("target_branch")
    String mTargetBranch;
    @SerializedName("source_branch")
    String mSourceBranch;
    @SerializedName("project_id")
    long mProjectId;
    @SerializedName("title")
    String mTitle;
    @SerializedName("state")
    String mState;
    @SerializedName("upvotes")
    long mUpvotes;
    @SerializedName("downvotes")
    long mDownvotes;
    @SerializedName("author")
    User mAuthor;
    @SerializedName("assignee")
    User mAssignee;
    @SerializedName("description")
    String mDescription;
    @SerializedName("updated_at")
    Date mUpdatedAt;
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("work_in_progress")
    boolean mWorkInProgress;

    public MergeRequest() {}

    public long getId() {
        return mId;
    }

    public long getIid() {
        return mIid;
    }

    public String getTargetBranch() {
        return mTargetBranch;
    }

    public String getSourceBranch() {
        return mSourceBranch;
    }

    public long getProjectId() {
        return mProjectId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getState() {
        return mState;
    }

    public long getUpvotes() {
        return mUpvotes;
    }

    public long getDownvotes() {
        return mDownvotes;
    }

    public User getAuthor() {
        return mAuthor;
    }

    public User getAssignee() {
        return mAssignee;
    }

    public String getDescription() {
        return mDescription;
    }

    public boolean isWorkInProgress() {
        return mWorkInProgress;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public Date getUpdatedAt() {
        return mUpdatedAt;
    }
}
