package com.commit451.gitlab.model.api;

import android.support.annotation.StringDef;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

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
    public @interface EditState {
    }

    public static final String STATE_OPENED = "opened";
    public static final String STATE_REOPENED = "reopened";
    public static final String STATE_CLOSED = "closed";

    @StringDef({STATE_OPENED, STATE_REOPENED, STATE_CLOSED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    @Json(name = "id")
    long id;
    @Json(name = "iid")
    long iId;
    @Json(name = "project_id")
    long projectId;
    @Json(name = "title")
    String title;
    @Json(name = "description")
    String description;
    @Json(name = "state")
    @State
    String state;
    @Json(name = "created_at")
    Date createdAt;
    @Json(name = "updated_at")
    Date updatedAt;
    @Json(name = "labels")
    List<String> labels;
    @Json(name = "milestone")
    Milestone milestone;
    @Json(name = "assignee")
    UserBasic assignee;
    @Json(name = "author")
    UserBasic author;
    @Json(name = "confidential")
    boolean confidential;

    public Issue() {
    }

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

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public List<String> getLabels() {
        return labels;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public UserBasic getAssignee() {
        return assignee;
    }

    public UserBasic getAuthor() {
        return author;
    }

    public boolean isConfidential() {
        return confidential;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Issue)) {
            return false;
        }

        Issue issue = (Issue) o;
        return id == issue.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
