package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class UserSafe {
    @Json(name = "name")
    String name;
    @Json(name = "username")
    String username;

    public UserSafe() {}

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserSafe)) {
            return false;
        }

        UserSafe user = (UserSafe) o;
        return username != null ? username.equals(user.username) : user.username == null;

    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}
