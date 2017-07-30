package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class ArtifactsFile {

    @Json(name = "filename")
    String fileName;
    @Json(name = "size")
    long size;

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }
}
