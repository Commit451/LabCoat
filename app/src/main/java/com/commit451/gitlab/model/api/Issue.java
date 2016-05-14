package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.net.Uri;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;

@Parcel
public class Issue {
    public static final String STATE_REOPEN = "reopen";
    public static final String STATE_CLOSE = "close";

    @StringDef({STATE_REOPEN, STATE_CLOSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EditState {}

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
    State mState;
    @JsonField(name = "created_at")
    Date mCreatedAt;
    @JsonField(name = "updated_at")
    Date mUpdatedAt;
    @JsonField(name = "labels")
    List<String> mLabels;
    @JsonField(name = "milestone")
    Milestone mMilestone;
    @JsonField(name = "assignee")
    UserBasic mAssignee;
    @JsonField(name = "author")
    UserBasic mAuthor;

    public Issue() {}

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

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public Date getUpdatedAt() {
        return mUpdatedAt;
    }

    public List<String> getLabels() {
        return mLabels;
    }

    public Milestone getMilestone() {
        return mMilestone;
    }

    public UserBasic getAssignee() {
        return mAssignee;
    }

    public UserBasic getAuthor() {
        return mAuthor;
    }

    public Uri getUrl(Project project) {
        return project.getWebUrl().buildUpon()
                .appendPath("issues")
                .appendPath(Long.toString(getId()))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Issue)) {
            return false;
        }

        Issue issue = (Issue) o;
        return mId == issue.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }

    public enum State {
        @JsonField(name = "opened")
        OPENED,
        @JsonField(name = "reopened")
        REOPENED,
        @JsonField(name = "closed")
        CLOSED
    }
}
