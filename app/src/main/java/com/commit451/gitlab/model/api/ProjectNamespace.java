package com.commit451.gitlab.model.api;

import android.net.Uri;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class ProjectNamespace {
    @Json(name = "id")
    long id;
    @Json(name = "name")
    String name;
    @Json(name = "path")
    String path;
    @Json(name = "owner_id")
    long ownerId;
    @Json(name = "created_at")
    Date createdAt;
    @Json(name = "updated_at")
    Date updatedAt;
    @Json(name = "description")
    String description;
    @Json(name = "avatar")
    Avatar avatar;
    @Json(name = "public")
    boolean isPublic;

    public ProjectNamespace() {
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProjectNamespace)) {
            return false;
        }

        ProjectNamespace that = (ProjectNamespace) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Parcel
    public static class Avatar {
        @Json(name = "url")
        Uri url;

        public Avatar() {
        }

        public Uri getUrl() {
            return url;
        }
    }
}
