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
    private String mName;
    @SerializedName("email")
    private String mEmail;
    @SerializedName("commits")
    private Integer mCommits;
    @SerializedName("additions")
    private Integer mAdditions;
    @SerializedName("deletions")
    private String mDeletions;

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
