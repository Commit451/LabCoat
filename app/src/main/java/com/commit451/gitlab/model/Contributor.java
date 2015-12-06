package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Contributor {
    @SerializedName("name")
    String mName;
    @SerializedName("email")
    String mEmail;
    @SerializedName("commits")
    int mCommits;
    @SerializedName("additions")
    int mAdditions;
    @SerializedName("deletions")
    int mDeletions;

    public Contributor() {}

    public String getName() {
        return mName;
    }

    public String getEmail() {
        return mEmail;
    }

    public int getCommits() {
        return mCommits;
    }

    public int getAdditions() {
        return mAdditions;
    }

    public int getDeletions() {
        return mDeletions;
    }
}
