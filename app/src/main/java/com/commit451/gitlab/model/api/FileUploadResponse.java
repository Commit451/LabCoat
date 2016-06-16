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
    protected String mAlt;
    @JsonField(name = "url")
    protected String mUrl;
    @JsonField(name = "is_image")
    protected boolean mIsImage;
    @JsonField(name = "markdown")
    protected String mMarkdown;

    protected FileUploadResponse() {
        //for json
    }

    public String getAlt() {
        return mAlt;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isImage() {
        return mIsImage;
    }

    public String getMarkdown() {
        return mMarkdown;
    }
}
