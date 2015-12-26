package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

@Parcel
public class Milestone {
    public static final String STATE_REOPEN = "reopen";
    public static final String STATE_CLOSE = "close";

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
    @SerializedName("state")
    State mState;
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("updated_at")
    Date mUpdatedAt;
    @SerializedName("due_date")
    Date mDueDate;

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

    public State getState() {
        return mState;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public Date getUpdatedAt() {
        return mUpdatedAt;
    }

    public Date getDueDate() {
        return mDueDate;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Milestone)) {
            return false;
        }

        Milestone milestone = (Milestone) o;
        return mId == milestone.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }

    public enum State {
        @SerializedName("opened")
        OPENED,
        @SerializedName("reopened")
        REOPENED,
        @SerializedName("closed")
        CLOSED
    }
}
