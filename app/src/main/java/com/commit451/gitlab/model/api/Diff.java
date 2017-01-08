package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

@Parcel
@JsonObject
public class Diff {
    @JsonField(name = "old_path")
    String oldPath;
    @JsonField(name = "new_path")
    String newPath;
    @JsonField(name = "a_mode")
    int aMode;
    @JsonField(name = "b_mode")
    int bMode;
    @JsonField(name = "diff")
    String diff;
    @JsonField(name = "new_file")
    boolean newFile;
    @JsonField(name = "renamed_file")
    boolean renamedFile;
    @JsonField(name = "deleted_file")
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
