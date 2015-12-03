package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.net.Uri;

/**
 * Avatar. Only used in Namespace for some reason
 * Created by Jawn on 9/22/2015.
 */
@Parcel
public class Avatar {
    @SerializedName("url")
    Uri url;

    public Avatar() {}
}
