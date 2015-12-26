package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import com.commit451.gitlab.R;

import org.parceler.Parcel;

import android.net.Uri;

@Parcel
public class RepositoryTreeObject {
    @SerializedName("id")
    String mId;
    @SerializedName("name")
    String mName;
    @SerializedName("type")
    Type mType;
    @SerializedName("mode")
    String mMode;

    public RepositoryTreeObject() {}

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public Type getType() {
        return mType;
    }

    public String getMode() {
        return mMode;
    }

    public int getDrawableForType() {
        switch (mType) {
            case FILE:
                return R.drawable.ic_file_24dp;
            case FOLDER:
                return R.drawable.ic_folder_24dp;
            case REPO:
                return R.drawable.ic_repo_24dp;
        }

        return R.drawable.ic_file_24dp;
    }

    public Uri getUrl(Project project, String branchName, String currentPath) {
        return project.getWebUrl().buildUpon()
                .appendPath("tree")
                .appendPath(branchName)
                .appendEncodedPath(currentPath)
                .appendPath(mName)
                .build();
    }

    public enum Type {
        @SerializedName("tree")
        FOLDER,
        @SerializedName("submodule")
        REPO,
        @SerializedName("blob")
        FILE
    }
}
