package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class RepositoryFile {
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
    @SerializedName("last_commit_id")
    String mLastCommitId;

    public RepositoryFile() {}

    public String getFileName() {
        return mFileName;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public long getSize() {
        return mSize;
    }

    public String getEncoding() {
        return mEncoding;
    }

    public String getContent() {
        return mContent;
    }

    public String getRef() {
        return mRef;
    }

    public String getBlobId() {
        return mBlobId;
    }

    public String getCommitId() {
        return mCommitId;
    }

    public String getLastCommitId() {
        return mLastCommitId;
    }
}
