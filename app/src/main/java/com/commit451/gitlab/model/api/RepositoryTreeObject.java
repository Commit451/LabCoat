package com.commit451.gitlab.model.api;

import android.net.Uri;

import com.commit451.gitlab.R;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

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
        if (mType == null) {
            return R.drawable.ic_file_24dp;
        }
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
