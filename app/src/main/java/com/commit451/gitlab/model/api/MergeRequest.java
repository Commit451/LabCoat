package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

@Parcel
public class MergeRequest {
    @SerializedName("id")
    long mId;
    @SerializedName("iid")
    long mIid;
    @SerializedName("project_id")
    long mProjectId;
    @SerializedName("title")
    String mTitle;
    @SerializedName("description")
    String mDescription;
    @SerializedName("state")
    State mState;
    @SerializedName("updated_at")
    Date mUpdatedAt;
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("target_branch")
    String mTargetBranch;
    @SerializedName("source_branch")
    String mSourceBranch;
    @SerializedName("upvotes")
    long mUpvotes;
    @SerializedName("downvotes")
    long mDownvotes;
    @SerializedName("author")
    UserBasic mAuthor;
    @SerializedName("assignee")
    UserBasic mAssignee;
    @SerializedName("source_project_id")
    long mSourceProjectId;
    @SerializedName("target_project_id")
    long mTargetProjectId;
    @SerializedName("labels")
    List<String> mLabels;
    @SerializedName("work_in_progress")
    boolean mWorkInProgress;
    @SerializedName("milestone")
    Milestone mMilestone;
    @SerializedName("merge_when_build_succeeds")
    boolean mMergeWhenBuildSucceeds;

    public MergeRequest() {}

    public long getId() {
        return mId;
    }

    public long getIid() {
        return mIid;
    }

    public long getProjectId() {
        return mProjectId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public State getState() {
        return mState;
    }

    public Date getUpdatedAt() {
        return mUpdatedAt;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public String getTargetBranch() {
        return mTargetBranch;
    }

    public String getSourceBranch() {
        return mSourceBranch;
    }

    public long getUpvotes() {
        return mUpvotes;
    }

    public long getDownvotes() {
        return mDownvotes;
    }

    public UserBasic getAuthor() {
        return mAuthor;
    }

    public UserBasic getAssignee() {
        return mAssignee;
    }

    public long getSourceProjectId() {
        return mSourceProjectId;
    }

    public long getTargetProjectId() {
        return mTargetProjectId;
    }

    public List<String> getLabels() {
        return mLabels;
    }

    public boolean isWorkInProgress() {
        return mWorkInProgress;
    }

    public Milestone getMilestone() {
        return mMilestone;
    }

    public boolean isMergeWhenBuildSucceedsEnabled() {
        return mMergeWhenBuildSucceeds;
    }

    public enum State {
        @SerializedName("opened")
        OPENED,
        @SerializedName("merged")
        MERGED,
        @SerializedName("closed")
        CLOSED
    }
}
