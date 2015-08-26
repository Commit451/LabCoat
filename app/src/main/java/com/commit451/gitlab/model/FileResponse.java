package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Jawn on 8/25/2015.
 */
@Parcel
public class FileResponse {

    @SerializedName("file_name")
    private String mFileName;
    @SerializedName("file_path")
    private String mFilePath;
    @SerializedName("size")
    private long mSize;
    @SerializedName("encoding")
    private String mEncoding;
    @SerializedName("content")
    private String mContent;
    @SerializedName("ref")
    private String mRef;
    @SerializedName("blob_id")
    private String mBlobId;
    @SerializedName("commit_id")
    private String mCommitId;

    public String getFileName() {
        return mFileName;
    }

    public String getContent() {
        return mContent;
    }
}
