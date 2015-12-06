package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class Namespace {
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("description")
    String mDescription;
    @SerializedName("id")
    long mId;
    @SerializedName("name")
    String mName;
    @SerializedName("owner_id")
    long mOwnerId;
    @SerializedName("updated_at")
    Date mUpdatedAt;
    @SerializedName("avatar")
    Avatar mAvatar;
    @SerializedName("membership_lock")
    boolean mMembershipLock;
    @SerializedName("share_with_group_lock")
    boolean mShareWithGroupLock;
    @SerializedName("public")
    boolean mPublic;

    public Namespace() {}

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public String getDescription() {
        return mDescription;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public long getOwnerId() {
        return mOwnerId;
    }

    public Date getUpdatedAt() {
        return mUpdatedAt;
    }

    public Avatar getAvatar() {
        return mAvatar;
    }

    public boolean getMembershipLock() {
        return mMembershipLock;
    }

    public boolean getShareWithGroupLock() {
        return mShareWithGroupLock;
    }

    public boolean isPublic() {
        return mPublic;
    }
}
