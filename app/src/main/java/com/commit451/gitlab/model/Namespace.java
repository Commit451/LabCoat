package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

/**
 * Namespace, which is like an organization (I think)
 * Created by Jawn on 9/22/2015.
 */
@Parcel
public class Namespace {
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("description")
    String mDescription;
    @SerializedName("id")
    Long mId;
    @SerializedName("name")
    String mName;
    @SerializedName("owner_id")
    Long mOwnerId;
    @SerializedName("updated_at")
    Date mUpdatedAt;
    @SerializedName("avatar")
    Avatar mAvatar;
    @SerializedName("membership_lock")
    Boolean mMembershipLock;

    public Namespace() {}

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public String getDescription() {
        return mDescription;
    }

    public Long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public Long getOwnerId() {
        return mOwnerId;
    }

    public Date getUpdatedAt() {
        return mUpdatedAt;
    }

    public Avatar getAvatar() {
        return mAvatar;
    }

    public Boolean getMembershipLock() {
        return mMembershipLock;
    }
}
