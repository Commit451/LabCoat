package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Diff {
    @SerializedName("old_path")
    String mOldPath;
    @SerializedName("new_path")
    String mNewPath;
    @SerializedName("a_mode")
    int mAMode;
    @SerializedName("b_mode")
    int mBMode;
    @SerializedName("diff")
    String mDiff;
    @SerializedName("new_file")
    boolean mNewFile;
    @SerializedName("renamed_file")
    boolean mRenamedFile;
    @SerializedName("deleted_file")
    boolean mDeletedFile;

    public Diff() {}

    public String getOldPath() {
        return mOldPath;
    }

    public String getNewPath() {
        return mNewPath;
    }

    public int getAMode() {
        return mAMode;
    }

    public int getBMode() {
        return mBMode;
    }

    public String getDiff() {
        return mDiff;
    }

    public boolean isNewFile() {
        return mNewFile;
    }

    public boolean isRenamedFile() {
        return mRenamedFile;
    }

    public boolean isDeletedFile() {
        return mDeletedFile;
    }

    public String getFileName() {
        if (mNewPath.contains("/")) {
            String[] paths = mNewPath.split("/");
            return paths[paths.length-1];
        } else {
            return mNewPath;
        }
    }
}
