package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * A label
 */
@Parcel
public class Label {

    @SerializedName("color")
    int mColor;

    protected Label() {
        //for json parsing
    }

}
