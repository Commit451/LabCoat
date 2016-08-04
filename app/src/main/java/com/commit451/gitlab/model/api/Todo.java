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
    String mId;
    @JsonField(name = "project")
    Project mProject;
    @JsonField(name = "author")
    UserBasic mAuthor;
    @JsonField(name = "action_name")
    String mActionName;
    @JsonField(name = "target_type")
    @TargetType
    String mTargetType;
    @JsonField(name = "target_url")
    String mTargetUrl;
    @JsonField(name = "body")
    String mBody;
    @JsonField(name = "state")
    @State
    String mState;
    @JsonField(name = "created_at")
    Date mCreatedAt;

    public Todo() {}

    public String getId() {
        return mId;
    }

    public Project getProject() {
        return mProject;
    }

    public UserBasic getAuthor() {
        return mAuthor;
    }

    public String getActionName() {
        return mActionName;
    }

    @TargetType
    public String getTargetType() {
        return mTargetType;
    }

    public String getTargetUrl() {
        return mTargetUrl;
    }

    public String getBody() {
        return mBody;
    }

    @State
    public String getState() {
        return mState;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }
}
