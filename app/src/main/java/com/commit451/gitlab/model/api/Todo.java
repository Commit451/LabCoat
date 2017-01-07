package com.commit451.gitlab.model.api;

import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

/**
 * Todos. Not processing Target, since it is different depending on what the type is, which
 * makes it not play nice with any automated json parsing
 */
@Parcel
@JsonObject
public class Todo {

    public static final String TARGET_ISSUE = "Issue";
    public static final String TARGET_MERGE_REQUEST = "MergeRequest";

    @StringDef({TARGET_ISSUE, TARGET_MERGE_REQUEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TargetType {
    }

    public static final String STATE_PENDING = "pending";
    public static final String STATE_DONE = "done";

    @StringDef({STATE_PENDING, STATE_DONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    @JsonField(name = "id")
    String id;
    @JsonField(name = "project")
    Project project;
    @JsonField(name = "author")
    UserBasic author;
    @JsonField(name = "action_name")
    String actionName;
    @JsonField(name = "target_type")
    @TargetType
    String targetType;
    @JsonField(name = "target_url")
    String targetUrl;
    @JsonField(name = "body")
    String body;
    @JsonField(name = "state")
    @State
    String state;
    @JsonField(name = "created_at")
    Date createdAt;

    public Todo() {}

    public String getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public UserBasic getAuthor() {
        return author;
    }

    public String getActionName() {
        return actionName;
    }

    @TargetType
    public String getTargetType() {
        return targetType;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getBody() {
        return body;
    }

    @State
    public String getState() {
        return state;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
