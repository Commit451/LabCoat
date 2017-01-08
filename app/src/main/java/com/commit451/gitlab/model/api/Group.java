package com.commit451.gitlab.model.api;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

@Parcel
@JsonObject
public class Group {
    @JsonField(name = "id")
    long id;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "path")
    String path;
    @JsonField(name = "description")
    String description;
    @JsonField(name = "avatar_url")
    Uri avatarUrl;
    @JsonField(name = "web_url")
    Uri webUrl;

    public Group() {}

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public Uri getAvatarUrl() {
        return avatarUrl;
    }

    public Uri getWebUrl() {
        return webUrl;
    }

    @Nullable
    public Uri getFeedUrl() {
        if (webUrl == null) {
            return null;
        }

        return Uri.parse(webUrl.toString() + ".atom");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Group)) {
            return false;
        }

        Group group = (Group) o;
        return id == group.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
