package com.commit451.gitlab.model.api;

import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.commit451.gitlab.api.converter.DueDateTypeConverter;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    String state;
    @JsonField(name = "created_at")
    Date createdAt;
    @JsonField(name = "updated_at")
    Date updatedAt;
    @JsonField(name = "due_date", typeConverter = DueDateTypeConverter.class)
    Date dueDate;

    public Milestone() {}

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

    @State
    public String getState() {
        return state;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    @Nullable
    public Date getDueDate() {
        return dueDate;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Milestone)) {
            return false;
        }

        Milestone milestone = (Milestone) o;
        return id == milestone.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return title;
    }
}
