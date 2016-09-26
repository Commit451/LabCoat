package com.commit451.gitlab.model.api;

import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

@Parcel
@JsonObject
public class Milestone {
    public static final String STATE_ACTIVE = "active";
    public static final String STATE_CLOSED = "closed";

    @StringDef({STATE_ACTIVE, STATE_CLOSED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    public static final String STATE_EVENT_ACTIVATE = "activate";
    public static final String STATE_EVENT_CLOSE = "close";

    @StringDef({STATE_EVENT_ACTIVATE, STATE_EVENT_CLOSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StateEvent {}

    public static final SimpleDateFormat DUE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-d", Locale.US);

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
    String mState;
    @JsonField(name = "created_at")
    Date mCreatedAt;
    @JsonField(name = "updated_at")
    Date mUpdatedAt;
    @JsonField(name = "due_date")
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


    @State
    public String getState() {
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
            Timber.e(e);
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

    @Override
    public String toString() {
        return mTitle;
    }
}
