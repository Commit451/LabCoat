package com.commit451.gitlab.model.api;

import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

@Parcel
public class Milestone {
    public static final String STATE_REOPEN = "reopen";
    public static final String STATE_CLOSE = "close";

    @StringDef({STATE_REOPEN, STATE_CLOSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EditState {}

    public static final SimpleDateFormat DUE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-d", Locale.US);

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
    String mDueDate;

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

    @Nullable
    public Date getDueDate() {
        if (TextUtils.isEmpty(mDueDate)) {
            return null;
        }
        try {
            return DUE_DATE_FORMAT.parse(mDueDate);
        } catch (ParseException e) {
            Timber.e(null, e);
        }
        return null;
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
