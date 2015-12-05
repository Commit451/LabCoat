package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;

@Parcel
public class MergeRequest {
    public static final String STATE_OPENED = "opened";
    public static final String STATE_MERGED = "merged";
    public static final String STATE_CLOSED = "closed";

    @StringDef({STATE_OPENED, STATE_MERGED, STATE_CLOSED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

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

    @State
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

    public Date getUpdatedAt() {
        return mUpdatedAt;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
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
}
