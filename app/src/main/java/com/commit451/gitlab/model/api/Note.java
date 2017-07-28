package com.commit451.gitlab.model.api;

import android.support.annotation.StringDef;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

@Parcel
public class Note {

    public static final String TYPE_ISSUE = "Issue";
    public static final String TYPE_MERGE_REQUEST = "MergeRequest";

    @StringDef({TYPE_ISSUE, TYPE_MERGE_REQUEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    @Json(name = "id")
    long id;
    @Json(name = "body")
    String body;
    @Json(name = "author")
    UserBasic author;
    @Json(name = "created_at")
    Date createdAt;
    @Json(name = "system")
    boolean system;
    @Json(name = "noteable_id")
    long noteableId;
    @Json(name = "noteable_type")
    @Type
    String noteableType;
    @Json(name = "upvote?")
    boolean upvote;
    @Json(name = "downvote?")
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
