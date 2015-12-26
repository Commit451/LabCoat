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
        return Uri.parse(mWebUrl.toString() + ".atom");
    }
}
