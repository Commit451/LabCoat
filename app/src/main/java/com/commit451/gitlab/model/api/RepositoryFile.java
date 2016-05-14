package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

@Parcel
public class RepositoryFile {
    @JsonField(name = "file_name")
    String mFileName;
    @JsonField(name = "file_path")
    String mFilePath;
    @JsonField(name = "size")
    long mSize;
    @JsonField(name = "encoding")
    String mEncoding;
    @JsonField(name = "content")
    String mContent;
    @JsonField(name = "ref")
    String mRef;
    @JsonField(name = "blob_id")
    String mBlobId;
    @JsonField(name = "commit_id")
    String mCommitId;
    @JsonField(name = "last_commit_id")
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
