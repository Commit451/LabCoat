package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class Note {
    @JsonField(name = "id")
    long mId;
    @JsonField(name = "body")
    String mBody;
    @JsonField(name = "author")
    UserBasic mAuthor;
    @JsonField(name = "created_at")
    Date mCreatedAt;
    @JsonField(name = "system")
    boolean mSystem;
    @JsonField(name = "noteable_id")
    long mNoteableId;
    @JsonField(name = "noteable_type")
    Type mNoteableType;
    @JsonField(name = "upvote")
    boolean mUpvote;
    @JsonField(name = "downvote")
    boolean mDownvote;

    public Note() {}

    public long getId() {
        return mId;
    }

    public String getBody() {
        return mBody;
    }

    public UserBasic getAuthor() {
        return mAuthor;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public boolean isSystem() {
        return mSystem;
    }

    public long getNoteableId() {
        return mNoteableId;
    }

    public Type getNoteableType() {
        return mNoteableType;
    }

    public boolean isUpvote() {
        return mUpvote;
    }

    public boolean isDownvote() {
        return mDownvote;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Note)) {
            return false;
        }

        Note note = (Note) o;
        return mId == note.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }

    public enum Type {
        @JsonField(name = "Issue")
        ISSUE,
        @JsonField(name = "MergeRequest")
        MERGE_REQUEST
    }
}
