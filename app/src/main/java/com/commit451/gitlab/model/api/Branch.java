package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import com.commit451.gitlab.util.ObjectHelper;

import org.parceler.Parcel;

@Parcel
public class Branch {
    @SerializedName("name")
    String mName;
    @SerializedName("protected")
    boolean mProtected;

    public Branch() {}

    public String getName() {
        return mName;
    }

    public boolean isProtected() {
        return mProtected;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Branch)) {
            return false;
        }

        Branch branch = (Branch) o;
        return ObjectHelper.equals(mName, branch.mName);
    }

    @Override
    public int hashCode() {
        return ObjectHelper.hash(mName);
    }

    @Override
    public String toString() {
        return mName;
    }
}
