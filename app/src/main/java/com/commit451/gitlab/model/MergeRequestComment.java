package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * A comment on a merge request
 * Created by Jawnnypoo on 11/17/2015.
 */
@Parcel
public class MergeRequestComment {

    @SerializedName("note")
    String mComment;
    @SerializedName("author")
    User mAuthor;

    public String getComment() {
        return mComment;
    }

    public User getAuthor() {
        return mAuthor;
    }
}
