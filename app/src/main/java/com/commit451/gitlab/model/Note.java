package com.commit451.gitlab.model;

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
    User mAuthor;
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("system")
    boolean mSystem;
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

    public User getAuthor() {
        return mAuthor;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public boolean isSystemNote() {
        return mSystem;
    }

    public boolean isUpvoted() {
        return mUpvote;
    }

    public boolean isDownvoted() {
        return mDownvote;
    }
}
