package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.util.Date;

/**
 * Represents a build
 */
@Parcel
@JsonObject
public class Build {

    @JsonField(name = "commit")
    RepositoryCommit commit;
    @JsonField(name = "coverage")
    String coverage;
    @JsonField(name = "created_at")
    Date createdAt;
    @JsonField(name = "artifacts_file")
    ArtifactsFile artifactsFile;
    @JsonField(name = "finished_at")
    Date finishedAt;
    @JsonField(name = "id")
    long id;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "ref")
    String ref;
    @JsonField(name = "runner")
    Runner runner;
    @JsonField(name = "stage")
    String stage;
    @JsonField(name = "started_at")
    Date startedAt;
    @JsonField(name = "status")
    String status;
    @JsonField(name = "tag")
    boolean tag;
    @JsonField(name = "user")
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
