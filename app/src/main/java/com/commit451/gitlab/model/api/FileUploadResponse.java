package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Response when a file is uploaded
 */
@Parcel
public class FileUploadResponse {

    @SerializedName("alt")
    protected String mAlt;
    @SerializedName("url")
    protected String mUrl;
    @SerializedName("is_image")
    protected boolean mIsImage;
    @SerializedName("markdown")
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
