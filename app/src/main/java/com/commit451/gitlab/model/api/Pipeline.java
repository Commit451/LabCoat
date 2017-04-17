package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

/**
 * A pipeline.
 */
@Parcel
@JsonObject
public class Pipeline {

    @JsonField(name = "id")
    long id;
    @JsonField(name = "sha")
    String sha;
    @JsonField(name = "ref")
    String ref;
    @JsonField(name = "status")
    String status;

    public long getId() {
        return id;
    }

    public String getSha() {
        return sha;
    }

    public String getRef() {
        return ref;
    }

    public String getStatus() {
        return status;
    }
}
