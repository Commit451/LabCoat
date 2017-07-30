package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

/**
 * Represents a pipeline
 */
@Parcel
public class Pipelines {

    @Json(name = "sha")
    String sha;
    @Json(name = "id")
    long id;
    @Json(name = "ref")
    String ref;
    @Json(name = "status")
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
