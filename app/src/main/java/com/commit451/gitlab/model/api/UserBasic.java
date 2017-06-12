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
    long id;
    @JsonField(name = "state")
    @State
    String state;
    @JsonField(name = "avatar_url")
    String avatarUrl;
    @JsonField(name = "web_url")
    String webUrl;

    public UserBasic() {}

    public long getId() {
        return id;
    }

    @State
    public String getState() {
        return state;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    @Nullable
    public Uri getFeedUrl() {
        if (webUrl == null) {
            return null;
        }
        return Uri.parse(webUrl + ".atom");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserBasic)) {
            return false;
        }

        UserBasic user = (UserBasic) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
