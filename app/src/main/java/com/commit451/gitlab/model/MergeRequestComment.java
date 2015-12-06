package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class MergeRequestComment {
    @SerializedName("note")
    String mComment;
    @SerializedName("author")
    User mAuthor;

    public MergeRequestComment() {}

    public String getComment() {
        return mComment;
    }

    public User getAuthor() {
        return mAuthor;
    }
}
