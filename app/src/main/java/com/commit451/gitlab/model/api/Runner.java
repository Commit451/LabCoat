package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

/**
 * A runner. It runs builds. yeah
 */
@Parcel
public class Runner {

    @Json(name = "id")
    long id;
    @Json(name = "description")
    String description;
    @Json(name = "active")
    boolean active;
    @Json(name = "is_shared")
    boolean isShared;
    @Json(name = "name")
    String name;

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isShared() {
        return isShared;
    }

    public String getName() {
        return name;
    }
}
