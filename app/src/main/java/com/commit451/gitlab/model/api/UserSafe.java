package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

@Parcel
public class UserSafe {
    @JsonField(name = "name")
    String mName;
    @JsonField(name = "username")
    String mUsername;

    public UserSafe() {}

    public String getName() {
        return mName;
    }

    public String getUsername() {
        return mUsername;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserSafe)) {
            return false;
        }

        UserSafe user = (UserSafe) o;
        return mUsername != null ? mUsername.equals(user.mUsername) : user.mUsername == null;

    }

    @Override
    public int hashCode() {
        return mUsername != null ? mUsername.hashCode() : 0;
    }
}
