package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Merge that request!
 * Created by Jawn on 9/20/2015.
 */
@Parcel
public class MergeRequest {
    @SerializedName("id")
    private long mId;
    @SerializedName("iid")
    private long mIid;
    @SerializedName("target_branch")
    private String mTargetBranch;
    @SerializedName("source_branch")
    private String mSourceBranch;
    @SerializedName("project_id")
    private long mProjectId;
    @SerializedName("title")
    private String mTitle;
    @SerializedName("state")
    private String mState;
    @SerializedName("upvotes")
    private long mUpvotes;
    @SerializedName("downvotes")
    private long mDownvotes;
    @SerializedName("author")
    private User mAuthor;
    @SerializedName("assignee")
    private User mAssignee;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("work_in_progress")
    private boolean mWorkInProgress;

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
}
