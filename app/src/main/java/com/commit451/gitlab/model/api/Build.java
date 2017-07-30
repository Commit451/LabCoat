package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.Date;

/**
 * Represents a build
 */
@Parcel
public class Build {

    @Json(name = "commit")
    RepositoryCommit commit;
    @Json(name = "coverage")
    String coverage;
    @Json(name = "created_at")
    Date createdAt;
    @Json(name = "artifacts_file")
    ArtifactsFile artifactsFile;
    @Json(name = "finished_at")
    Date finishedAt;
    @Json(name = "id")
    long id;
    @Json(name = "name")
    String name;
    @Json(name = "ref")
    String ref;
    @Json(name = "runner")
    Runner runner;
    @Json(name = "pipeline")
    Pipeline pipeline;
    @Json(name = "stage")
    String stage;
    @Json(name = "started_at")
    Date startedAt;
    @Json(name = "status")
    String status;
    @Json(name = "tag")
    boolean tag;
    @Json(name = "user")
    User user;

    public RepositoryCommit getCommit() {
        return commit;
    }

    public String getCoverage() {
        return coverage;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public ArtifactsFile getArtifactsFile() {
        return artifactsFile;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public Runner getRunner() {
        return runner;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public String getStage() {
        return stage;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public String getStatus() {
        return status;
    }

    public boolean isTag() {
        return tag;
    }

    public User getUser() {
        return user;
    }
}
