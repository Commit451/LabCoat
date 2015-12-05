package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

@Parcel
public class Milestone {
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
    @SerializedName("due_date")
    Date mDueDate;
    @SerializedName("state")
    String mState;
    @SerializedName("updated_at")
    Date mUpdatedAt;
    @SerializedName("created_at")
    Date mCreatedAt;

    public Milestone() {}

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

    public Date getDueDate() {
        return mDueDate;
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

    public boolean equals(Object obj) {
        if (!(obj instanceof Milestone)) {
            return false;
        }

        Milestone rhs = (Milestone) obj;
        return rhs.mId == mId;
    }
}
