package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.commit451.gitlab.util.ObjectUtil;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
@JsonObject
public class CommitUser {
    @JsonField(name = "id")
    String id;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "username")
    String username;
    @JsonField(name = "state")
    String state;
    @JsonField(name = "avatar_url")
    String avatarUrl;
    @JsonField(name = "web_url")
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
