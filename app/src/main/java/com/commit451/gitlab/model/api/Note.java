package com.commit451.gitlab.model.api;

import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

@Parcel
@JsonObject
public class Note {

    public static final String TYPE_ISSUE = "Issue";
    public static final String TYPE_MERGE_REQUEST = "MergeRequest";

    @StringDef({TYPE_ISSUE, TYPE_MERGE_REQUEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    @JsonField(name = "id")
    long id;
    @JsonField(name = "body")
    String body;
    @JsonField(name = "author")
    UserBasic author;
    @JsonField(name = "created_at")
    Date createdAt;
    @JsonField(name = "system")
    boolean system;
    @JsonField(name = "noteable_id")
    long noteableId;
    @JsonField(name = "noteable_type")
    @Type
    String noteableType;
    @JsonField(name = "upvote?")
    boolean upvote;
    @JsonField(name = "downvote?")
    boolean downvote;

    public Note() {}

    public long getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public UserBasic getAuthor() {
        return author;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean isSystem() {
        return system;
    }

    public long getNoteableId() {
        return noteableId;
    }

    public @Type String getNoteableType() {
        return noteableType;
    }

    public boolean isUpvote() {
        return upvote;
    }

    public boolean isDownvote() {
        return downvote;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Note)) {
            return false;
        }

        Note note = (Note) o;
        return id == note.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
