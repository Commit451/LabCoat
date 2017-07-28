package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

/**
 * Response when a file is uploaded
 */
@Parcel
public class FileUploadResponse {

    @Json(name = "alt")
    String alt;
    @Json(name = "url")
    String url;
    @Json(name = "is_image")
    boolean isImage;
    @Json(name = "markdown")
    String markdown;

    protected FileUploadResponse() {
        //for json
    }

    public String getAlt() {
        return alt;
    }

    public String getUrl() {
        return url;
    }

    public boolean isImage() {
        return isImage;
    }

    public String getMarkdown() {
        return markdown;
    }
}
