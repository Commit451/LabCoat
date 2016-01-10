package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.net.Uri;

@Parcel
public class UserBasic extends UserSafe {
    @SerializedName("id")
    long mId;
    @SerializedName("state")
    State mState;
    @SerializedName("avatar_url")
    Uri mAvatarUrl;
    @SerializedName("web_url")
    Uri mWebUrl;

    public UserBasic() {}

    public long getId() {
        return mId;
    }

    public State getState() {
        return mState;
    }

    public Uri getAvatarUrl() {
        return mAvatarUrl;
    }

    public Uri getWebUrl() {
        return mWebUrl;
    }

    public Uri getFeedUrl() {
        return Uri.parse(mWebUrl.toString() + ".atom");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserBasic)) {
            return false;
        }

        UserBasic user = (UserBasic) o;
        return mId == user.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }

    public enum State {
        @SerializedName("active")
        ACTIVE,
        @SerializedName("blocked")
        BLOCKED
    }
}
