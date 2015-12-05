package com.commit451.gitlab.model;

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
    public static final String STATE_OPENED = "opened";
    public static final String STATE_REOPENED = "reopened";
    public static final String STATE_CLOSED = "closed";

    public static final String STATE_REOPEN = "reopen";
    public static final String STATE_CLOSE = "close";

    @StringDef({STATE_OPENED, STATE_REOPENED, STATE_CLOSED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    @StringDef({STATE_REOPEN, STATE_CLOSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EditState {}

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
    @SerializedName("labels")
    List<String> mLabels;
    @SerializedName("milestone")
    Milestone mMilestone;
    @SerializedName("assignee")
    User mAssignee;
    @SerializedName("author")
    User mAuthor;
    @SerializedName("state")
    String mState;
    @SerializedName("updated_at")
    Date mUpdatedAt;
    @SerializedName("created_at")
    Date mCreatedAt;

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

    public List<String> getLabels() {
        return mLabels;
    }

    public Milestone getMilestone() {
        return mMilestone;
    }

    public User getAssignee() {
        return mAssignee;
    }

    public User getAuthor() {
        return mAuthor;
    }

    @State
    public String getState() {
        return mState;
    }

    public Date getUpdatedAt() {
        return mUpdatedAt;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public Uri getUrl(Project project) {
        return project.getWebUrl().buildUpon()
                .appendPath("issues")
                .appendPath(Long.toString(getId()))
                .build();
    }
}
