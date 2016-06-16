package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

/**
 * A runner. It runs builds. yeah
 */
@Parcel
@JsonObject
public class Runner {

    @JsonField(name = "id")
    long mId;
    @JsonField(name = "description")
    String mDescription;
    @JsonField(name = "active")
    boolean mActive;
    @JsonField(name = "is_shared")
    boolean mIsShared;
    @JsonField(name = "name")
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
