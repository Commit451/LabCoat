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
    long mId;
    @JsonField(name = "name")
    String mName;
    @JsonField(name = "path")
    String mPath;
    @JsonField(name = "owner_id")
    long mOwnerId;
    @JsonField(name = "created_at")
    Date mCreatedAt;
    @JsonField(name = "updated_at")
    Date mUpdatedAt;
    @JsonField(name = "description")
    String mDescription;
    @JsonField(name = "avatar")
    Avatar mAvatar;
    @JsonField(name = "public")
    boolean mPublic;

    public ProjectNamespace() {
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getPath() {
        return mPath;
    }

    public long getOwnerId() {
        return mOwnerId;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public Date getUpdatedAt() {
        return mUpdatedAt;
    }

    public String getDescription() {
        return mDescription;
    }

    public Avatar getAvatar() {
        return mAvatar;
    }

    public boolean isPublic() {
        return mPublic;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProjectNamespace)) {
            return false;
        }

        ProjectNamespace that = (ProjectNamespace) o;
        return mId == that.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }

    @Parcel
    @JsonObject
    public static class Avatar {
        @JsonField(name = "url")
        Uri mUrl;

        public Avatar() {
        }

        public Uri getUrl() {
            return mUrl;
        }
    }
}
