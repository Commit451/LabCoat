package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

@Parcel
@JsonObject
public class RepositoryFile {
    @JsonField(name = "file_name")
    String fileName;
    @JsonField(name = "file_path")
    String filePath;
    @JsonField(name = "size")
    long size;
    @JsonField(name = "encoding")
    String encoding;
    @JsonField(name = "content")
    String content;
    @JsonField(name = "ref")
    String ref;
    @JsonField(name = "blob_id")
    String blobId;
    @JsonField(name = "commit_id")
    String commitId;
    @JsonField(name = "last_commit_id")
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
