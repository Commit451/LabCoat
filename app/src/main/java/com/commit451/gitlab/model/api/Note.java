package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class Note {
    @SerializedName("id")
    long mId;
    @SerializedName("body")
    String mBody;
    @SerializedName("author")
    UserBasic mAuthor;
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("system")
    boolean mSystem;
    @SerializedName("noteable_id")
    long mNoteableId;
    @SerializedName("noteable_type")
    Type mNoteableType;
    @SerializedName("upvote")
    boolean mUpvote;
    @SerializedName("downvote")
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
        @SerializedName("Issue")
        ISSUE,
        @SerializedName("MergeRequest")
        MERGE_REQUEST
    }
}
