package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Jawn on 8/25/2015.
 */
@Parcel
public class FileResponse {

    @SerializedName("file_name")
    String mFileName;
    @SerializedName("file_path")
    String mFilePath;
    @SerializedName("size")
    long mSize;
    @SerializedName("encoding")
    String mEncoding;
    @SerializedName("content")
    String mContent;
    @SerializedName("ref")
    String mRef;
    @SerializedName("blob_id")
    String mBlobId;
    @SerializedName("commit_id")
    String mCommitId;

    public String getFileName() {
        return mFileName;
    }

    public String getContent() {
        return mContent;
    }
}
