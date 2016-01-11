package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.net.Uri;

@Parcel
public class Group {
    @SerializedName("id")
    long mId;
    @SerializedName("name")
    String mName;
    @SerializedName("path")
    String mPath;
    @SerializedName("description")
    String mDescription;
    @SerializedName("avatar_url")
    Uri mAvatarUrl;
    @SerializedName("web_url")
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
