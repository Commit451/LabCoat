package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

/**
 * Artifact from a build
 */
@Parcel
@JsonObject
public class Artifact {
    @JsonField(name = "name")
    String mName;

    public String getName() {
        return mName;
    }
}
