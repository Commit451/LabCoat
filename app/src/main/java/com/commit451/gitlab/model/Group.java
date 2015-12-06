package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.net.Uri;

import java.util.List;

@Parcel
public class Group {
    @SerializedName("id")
    long mId;
    @SerializedName("name")
    String mName;
    @SerializedName("description")
    String mDescription;
    @SerializedName("path")
    String mPath;
    @SerializedName("avatar_url")
    Uri mAvatarUrl;
    @SerializedName("web_url")
    Uri mWebUrl;
    @SerializedName("projects")
    List<Project> mProjects;

    public Group() {}

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getPath() {
        return mPath;
    }

    public Uri getAvatarUrl() {
        return mAvatarUrl;
    }

    public Uri getWebUrl() {
        return mWebUrl;
    }

    public List<Project> getProjects() {
        return mProjects;
    }
}
