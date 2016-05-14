package com.commit451.gitlab.model.api;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

@Parcel
public class Group {
    @JsonField(name = "id")
    long mId;
    @JsonField(name = "name")
    String mName;
    @JsonField(name = "path")
    String mPath;
    @JsonField(name = "description")
    String mDescription;
    @JsonField(name = "avatar_url")
    Uri mAvatarUrl;
    @JsonField(name = "web_url")
    Uri mWebUrl;

    public Group() {}

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getPath() {
        return mPath;
    }

    public String getDescription() {
        return mDescription;
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
        if (!(o instanceof Group)) {
            return false;
        }

        Group group = (Group) o;
        return mId == group.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }
}
