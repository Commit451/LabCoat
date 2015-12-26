package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import com.commit451.gitlab.util.ObjectUtil;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Contributor)) {
            return false;
        }

        Contributor contributor = (Contributor) o;
        return ObjectUtil.equals(mName, contributor.mName) && ObjectUtil.equals(mEmail, contributor.mEmail);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hash(mName, mEmail);
    }
}
