package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import com.commit451.gitlab.R;

import org.parceler.Parcel;

import android.net.Uri;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Parcel
public class TreeItem {
    public static final String TYPE_FOLDER = "tree";
    public static final String TYPE_REPO = "submodule";
    public static final String TYPE_FILE = "blob";

    @StringDef({TYPE_FOLDER, TYPE_REPO, TYPE_FILE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    @SerializedName("name")
    String mName;
    @SerializedName("type")
    String mType;
    @SerializedName("mode")
    long mMode;
    @SerializedName("id")
    String mId;

    public TreeItem() {}

    public String getName() {
        return mName;
    }

    @Type
    public String getType() {
        return mType;
    }

    public long getMode() {
        return mMode;
    }

    public String getId() {
        return mId;
    }

    public int getDrawableForType() {
        switch (mType) {
            case TYPE_FILE:
                return R.drawable.ic_file_24dp;
            case TYPE_FOLDER:
                return R.drawable.ic_folder_24dp;
            case TYPE_REPO:
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
}
