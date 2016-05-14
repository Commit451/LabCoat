package com.commit451.gitlab.model.api;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

@Parcel
public class UserBasic extends UserSafe {
    @JsonField(name = "id")
    long mId;
    @JsonField(name = "state")
    State mState;
    @JsonField(name = "avatar_url")
    Uri mAvatarUrl;
    @JsonField(name = "web_url")
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

    @Nullable
    public Uri getFeedUrl() {
        if (mWebUrl == null) {
            return null;
        }
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
        @JsonField(name = "active")
        ACTIVE,
        @JsonField(name = "blocked")
        BLOCKED
    }
}
