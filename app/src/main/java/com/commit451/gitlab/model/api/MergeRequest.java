package com.commit451.gitlab.model.api;

import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;

@Parcel
@JsonObject
public class MergeRequest {

    public static final String STATE_OPENED = "opened";
    public static final String STATE_MERGED = "merged";
    public static final String STATE_CLOSED = "closed";

    @StringDef({STATE_OPENED, STATE_MERGED, STATE_CLOSED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    @JsonField(name = "id")
    long mId;
    @JsonField(name = "iid")
    long mIid;
    @JsonField(name = "project_id")
    long mProjectId;
    @JsonField(name = "title")
    String mTitle;
    @JsonField(name = "description")
    String mDescription;
    @JsonField(name = "state")
    @State
    String mState;
    @JsonField(name = "updated_at")
    Date mUpdatedAt;
    @JsonField(name = "created_at")
    Date mCreatedAt;
    @JsonField(name = "target_branch")
    String mTargetBranch;
    @JsonField(name = "source_branch")
    String mSourceBranch;
    @JsonField(name = "upvotes")
    long mUpvotes;
    @JsonField(name = "downvotes")
    long mDownvotes;
    @JsonField(name = "author")
    UserBasic mAuthor;
    @JsonField(name = "assignee")
    UserBasic mAssignee;
    @JsonField(name = "source_project_id")
    long mSourceProjectId;
    @JsonField(name = "target_project_id")
    long mTargetProjectId;
    @JsonField(name = "labels")
    List<String> mLabels;
    @JsonField(name = "work_in_progress")
    boolean mWorkInProgress;
    @JsonField(name = "milestone")
    Milestone mMilestone;
    @JsonField(name = "merge_when_build_succeeds")
    boolean mMergeWhenBuildSucceeds;
    @JsonField(name = "merge_status")
    String mMergeStatus;
    @JsonField(name = "changes")
    @Nullable
    List<Diff> mChanges;

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

    public @State String getState() {
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

    public String getMergeStatus() {
        return mMergeStatus;
    }

    /**
     * Get the changes. Only not null if this merge request was retrieved via {@link com.commit451.gitlab.api.GitLab#getMergeRequestChanges(long, long)}
     * @return the changes
     */
    @Nullable
    public List<Diff> getChanges() {
        return mChanges;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MergeRequest)) {
            return false;
        }

        MergeRequest mergeRequest = (MergeRequest) o;
        return mId == mergeRequest.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }
}
