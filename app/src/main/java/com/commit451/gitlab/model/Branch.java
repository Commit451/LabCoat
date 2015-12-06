package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

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
    public String toString() {
        return mName;
    }
}
