package com.commit451.gitlab.model;

import android.net.Uri;
import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
@Parcel
public class Issue {

    @StringDef({STATE_REOPEN, STATE_CLOSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}
    public static final String STATE_REOPENED = "reopened";
    public static final String STATE_CLOSED = "closed";
    public static final String STATE_ACTIVE = "active";
    public static final String STATE_OPENED = "opened";

    @StringDef({STATE_REOPEN, STATE_CLOSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EditState {}
    public static final String STATE_REOPEN = "reopen";
    public static final String STATE_CLOSE = "close";


    @SerializedName("id")
    long id;
    @SerializedName("iid")
    long iid;
    @SerializedName("project_id")
    long project_id;
    @SerializedName("title")
    String title;
    @SerializedName("description")
    String description;
    @SerializedName("labels")
    String[] labels;
    @SerializedName("milestone")
    Milestone milestone;
    @SerializedName("assignee")
    User assignee;
    @SerializedName("author")
    User author;
    @SerializedName("state")
    String state;
    @SerializedName("updated_at")
    Date updated_at;
    @SerializedName("created_at")
    Date created_at;

    public Issue(){}

    public long getId() {
        return id;
    }

    public long getIid() {
        return iid;
    }

    public long getProjectId() {
        return project_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String[] getLabels() {
        return labels;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public User getAssignee() {
        return assignee;
    }

    public User getAuthor() {
        return author;
    }

    @State
    public String getState() {
        return state;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public Uri getUrl(Project project) {
        return project.getWebUrl().buildUpon()
                .appendPath("issues")
                .appendPath(Long.toString(getId()))
                .build();
    }
}
