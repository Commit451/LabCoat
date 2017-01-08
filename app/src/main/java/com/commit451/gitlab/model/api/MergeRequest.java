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
    long id;
    @JsonField(name = "iid")
    long iId;
    @JsonField(name = "project_id")
    long projectId;
    @JsonField(name = "title")
    String title;
    @JsonField(name = "description")
    String description;
    @JsonField(name = "state")
    @State
    String state;
    @JsonField(name = "updated_at")
    Date updatedAt;
    @JsonField(name = "created_at")
    Date createdAt;
    @JsonField(name = "target_branch")
    String targetBranch;
    @JsonField(name = "source_branch")
    String sourceBranch;
    @JsonField(name = "upvotes")
    long upvotes;
    @JsonField(name = "downvotes")
    long downvotes;
    @JsonField(name = "author")
    UserBasic author;
    @JsonField(name = "assignee")
    UserBasic assignee;
    @JsonField(name = "source_project_id")
    long sourceProjectId;
    @JsonField(name = "target_project_id")
    long targetProjectId;
    @JsonField(name = "labels")
    List<String> labels;
    @JsonField(name = "work_in_progress")
    boolean workInProgress;
    @JsonField(name = "milestone")
    Milestone milestone;
    @JsonField(name = "merge_when_build_succeeds")
    boolean mergeWhenBuildSucceeds;
    @JsonField(name = "merge_status")
    String mergeStatus;
    @JsonField(name = "changes")
    @Nullable
    List<Diff> changes;

    public MergeRequest() {}

    public long getId() {
        return id;
    }

    public long getIid() {
        return iId;
    }

    public long getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public @State String getState() {
        return state;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public long getUpvotes() {
        return upvotes;
    }

    public long getDownvotes() {
        return downvotes;
    }

    public UserBasic getAuthor() {
        return author;
    }

    public UserBasic getAssignee() {
        return assignee;
    }

    public long getSourceProjectId() {
        return sourceProjectId;
    }

    public long getTargetProjectId() {
        return targetProjectId;
    }

    public List<String> getLabels() {
        return labels;
    }

    public boolean isWorkInProgress() {
        return workInProgress;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public boolean isMergeWhenBuildSucceedsEnabled() {
        return mergeWhenBuildSucceeds;
    }

    public String getMergeStatus() {
        return mergeStatus;
    }

    /**
     * Get the changes. Only not null if this merge request was retrieved via {@link com.commit451.gitlab.api.GitLab#getMergeRequestChanges(long, long)}
     * @return the changes
     */
    @Nullable
    public List<Diff> getChanges() {
        return changes;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MergeRequest)) {
            return false;
        }

        MergeRequest mergeRequest = (MergeRequest) o;
        return id == mergeRequest.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
