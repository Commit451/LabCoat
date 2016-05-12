package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Artifact from a build
 */
@Parcel
public class Artifact {
    @SerializedName("name")
    String mName;

    public String getName() {
        return mName;
    }
}
