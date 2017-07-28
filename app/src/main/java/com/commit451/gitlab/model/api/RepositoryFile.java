package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class RepositoryFile {
    @Json(name = "file_name")
    String fileName;
    @Json(name = "file_path")
    String filePath;
    @Json(name = "size")
    long size;
    @Json(name = "encoding")
    String encoding;
    @Json(name = "content")
    String content;
    @Json(name = "ref")
    String ref;
    @Json(name = "blob_id")
    String blobId;
    @Json(name = "commit_id")
    String commitId;
    @Json(name = "last_commit_id")
    String lastCommitId;

    public RepositoryFile() {}

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getSize() {
        return size;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getContent() {
        return content;
    }

    public String getRef() {
        return ref;
    }

    public String getBlobId() {
        return blobId;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getLastCommitId() {
        return lastCommitId;
    }
}
