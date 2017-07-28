package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.Date;

/**
 * A pipeline.
 */
@Parcel
public class Pipeline {

    @Json(name = "user")
    CommitUser user;
    @Json(name = "id")
    long id;
    @Json(name = "sha")
    String sha;
    @Json(name = "ref")
    String ref;
    @Json(name = "status")
    String status;
    @Json(name = "created_at")
    Date createdAt;
    @Json(name = "started_at")
    Date startedAt;
    @Json(name = "finished_at")
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
