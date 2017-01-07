package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

/**
 * A runner. It runs builds. yeah
 */
@Parcel
@JsonObject
public class Runner {

    @JsonField(name = "id")
    long id;
    @JsonField(name = "description")
    String description;
    @JsonField(name = "active")
    boolean active;
    @JsonField(name = "is_shared")
    boolean isShared;
    @JsonField(name = "name")
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
