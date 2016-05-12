package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * A runner. It runs builds. yeah
 */
@Parcel
public class Runner {

    @SerializedName("id")
    long mId;
    @SerializedName("description")
    String mDescription;
    @SerializedName("active")
    boolean mActive;
    @SerializedName("is_shared")
    boolean mIsShared;
    @SerializedName("name")
    String mName;

    public long getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

    public boolean isActive() {
        return mActive;
    }

    public boolean isShared() {
        return mIsShared;
    }

    public String getName() {
        return mName;
    }
}
