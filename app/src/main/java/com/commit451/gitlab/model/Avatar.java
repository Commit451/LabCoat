package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.net.Uri;

@Parcel
public class Avatar {
    @SerializedName("url")
    Uri mUrl;

    public Avatar() {}

    public Uri getUrl() {
        return mUrl;
    }
}
