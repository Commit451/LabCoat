package com.commit451.gitlab.model.api;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Parcel
@JsonObject
public class UserBasic extends UserSafe {

    public static final String STATE_ACTIVE = "active";
    public static final String STATE_BLOCKED = "blocked";

    @StringDef({STATE_ACTIVE, STATE_BLOCKED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    @JsonField(name = "id")
    long mId;
    @JsonField(name = "state")
    @State
    String mState;
    @JsonField(name = "avatar_url")
    Uri mAvatarUrl;
    @JsonField(name = "web_url")
    Uri mWebUrl;

    public UserBasic() {}

    public long getId() {
        return mId;
    }

    public @State String getState() {
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
}
