package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

@JsonObject
@Parcel
public class ArtifactsFile {

    @JsonField(name = "filename")
    String fileName;
    @JsonField(name = "size")
    long size;

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }
}
