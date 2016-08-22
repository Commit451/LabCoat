package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

@JsonObject
@Parcel
public class ArtifactsFile {

    @JsonField(name = "filename")
    String mFileName;
    @JsonField(name = "size")
    long mSize;

    public String getFileName() {
        return mFileName;
    }

    public long getSize() {
        return mSize;
    }
}
