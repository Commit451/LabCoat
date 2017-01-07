package com.commit451.gitlab.model.api;

import android.net.Uri;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
@JsonObject
public class ProjectNamespace {
    @JsonField(name = "id")
    long id;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "path")
    String path;
    @JsonField(name = "owner_id")
    long ownerId;
    @JsonField(name = "created_at")
    Date createdAt;
    @JsonField(name = "updated_at")
    Date updatedAt;
    @JsonField(name = "description")
    String description;
    @JsonField(name = "avatar")
    Avatar avatar;
    @JsonField(name = "public")
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
    @JsonObject
    public static class Avatar {
        @JsonField(name = "url")
        Uri url;

        public Avatar() {
        }

        public Uri getUrl() {
            return url;
        }
    }
}
