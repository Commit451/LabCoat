package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import android.net.Uri;

import java.util.Date;

@Parcel
public class ProjectNamespace {
    @SerializedName("id")
    long mId;
    @SerializedName("name")
    String mName;
    @SerializedName("path")
    String mPath;
    @SerializedName("owner_id")
    long mOwnerId;
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("updated_at")
    Date mUpdatedAt;
    @SerializedName("description")
    String mDescription;
    @SerializedName("avatar")
    Avatar mAvatar;
    @SerializedName("public")
    boolean mPublic;

    public ProjectNamespace() {}

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
    public static class Avatar {
        @SerializedName("url")
        Uri mUrl;

        public Avatar() {}

        public Uri getUrl() {
            return mUrl;
        }
    }
}
