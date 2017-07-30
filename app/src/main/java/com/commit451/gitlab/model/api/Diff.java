package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class Diff {
    @Json(name = "old_path")
    String oldPath;
    @Json(name = "new_path")
    String newPath;
    @Json(name = "a_mode")
    int aMode;
    @Json(name = "b_mode")
    int bMode;
    @Json(name = "diff")
    String diff;
    @Json(name = "new_file")
    boolean newFile;
    @Json(name = "renamed_file")
    boolean renamedFile;
    @Json(name = "deleted_file")
    boolean deletedFile;

    public Diff() {}

    public String getOldPath() {
        return oldPath;
    }

    public String getNewPath() {
        return newPath;
    }

    public int getAMode() {
        return aMode;
    }

    public int getBMode() {
        return bMode;
    }

    public String getDiff() {
        return diff;
    }

    public boolean isNewFile() {
        return newFile;
    }

    public boolean isRenamedFile() {
        return renamedFile;
    }

    public boolean isDeletedFile() {
        return deletedFile;
    }

    public String getFileName() {
        if (newPath.contains("/")) {
            String[] paths = newPath.split("/");
            return paths[paths.length-1];
        } else {
            return newPath;
        }
    }
}
