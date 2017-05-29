package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.util.Date;

/**
 * A pipeline.
 */
@Parcel
@JsonObject
public class Pipeline {

    @JsonField(name = "user")
    CommitUser user;
    @JsonField(name = "id")
    long id;
    @JsonField(name = "sha")
    String sha;
    @JsonField(name = "ref")
    String ref;
    @JsonField(name = "status")
    String status;
    @JsonField(name = "created_at")
    Date createdAt;
    @JsonField(name = "started_at")
    Date startedAt;
    @JsonField(name = "finished_at")
    Date finishedAt;

    public CommitUser getUser() {
        return user;
    }
    public long getId() {
        return id;
    }

    public String getSha() {
        return sha;
    }

    public String getRef() {
        return ref;
    }

    public String getStatus() {
        return status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getStartedAt() { return startedAt; }

    public Date getFinishedAt() { return finishedAt; }
}
