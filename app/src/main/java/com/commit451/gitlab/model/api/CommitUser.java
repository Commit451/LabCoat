package com.commit451.gitlab.model.api;

import com.commit451.gitlab.util.ObjectUtil;
import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class CommitUser {
    @Json(name = "id")
    String id;
    @Json(name = "name")
    String name;
    @Json(name = "username")
    String username;
    @Json(name = "state")
    String state;
    @Json(name = "avatar_url")
    String avatarUrl;
    @Json(name = "web_url")
    String webUrl;

    public CommitUser() {}

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getState() {
        return state;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CommitUser)) {
            return false;
        }

        CommitUser commit = (CommitUser) o;
        return ObjectUtil.INSTANCE.equals(id, commit.id);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.INSTANCE.hash(id);
    }
}
