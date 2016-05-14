package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

@Parcel
public class Diff {
    @JsonField(name = "old_path")
    String mOldPath;
    @JsonField(name = "new_path")
    String mNewPath;
    @JsonField(name = "a_mode")
    int mAMode;
    @JsonField(name = "b_mode")
    int mBMode;
    @JsonField(name = "diff")
    String mDiff;
    @JsonField(name = "new_file")
    boolean mNewFile;
    @JsonField(name = "renamed_file")
    boolean mRenamedFile;
    @JsonField(name = "deleted_file")
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
