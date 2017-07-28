package com.commit451.gitlab.model.api;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class Group {
    @Json(name = "id")
    long id;
    @Json(name = "name")
    String name;
    @Json(name = "path")
    String path;
    @Json(name = "description")
    String description;
    @Json(name = "avatar_url")
    String avatarUrl;
    @Json(name = "web_url")
    String webUrl;

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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    @Nullable
    public Uri getFeedUrl() {
        if (webUrl == null) {
            return null;
        }

        return Uri.parse(webUrl + ".atom");
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
