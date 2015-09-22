package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Contributor to a repo
 * Created by Jawn on 9/20/2015.
 */
@Parcel
public class Contributor {
    @SerializedName("name")
    String mName;
    @SerializedName("email")
    String mEmail;
    @SerializedName("commits")
    Integer mCommits;
    @SerializedName("additions")
    Integer mAdditions;
    @SerializedName("deletions")
    String mDeletions;

    public Contributor() {}

    public String getName() {
        return mName;
    }

    public String getEmail() {
        return mEmail;
    }

    public Integer getCommits() {
        return mCommits;
    }

    public Integer getAdditions() {
        return mAdditions;
    }

    public String getDeletions() {
        return mDeletions;
    }
}
