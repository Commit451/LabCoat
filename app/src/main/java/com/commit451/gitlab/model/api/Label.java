package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

/**
 * A label
 */
@Parcel
@JsonObject
public class Label {

    @JsonField(name = "color")
    int mColor;

    protected Label() {
        //for json parsing
    }

}
