package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

/**
 * Response when a file is uploaded
 */
@Parcel
@JsonObject
public class FileUploadResponse {

    @JsonField(name = "alt")
    String alt;
    @JsonField(name = "url")
    String url;
    @JsonField(name = "is_image")
    boolean isImage;
    @JsonField(name = "markdown")
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
