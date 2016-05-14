package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

import java.util.Date;

/**
 * Represents a build
 */
@Parcel
public class Build {

    @JsonField(name = "commit")
    RepositoryCommit mCommit;
    @JsonField(name = "coverage")
    String mCoverage;
    @JsonField(name = "created_at")
    Date mCreatedAt;
    @JsonField(name = "artifacts_file")
    String mArtifactsFile;
    @JsonField(name = "finished_at")
    Date mFinishedAt;
    @JsonField(name = "id")
    long mId;
    @JsonField(name = "name")
    String mName;
    @JsonField(name = "ref")
    String mRef;
    @JsonField(name = "runner")
    Runner mRunner;
    @JsonField(name = "stage")
    String mStage;
    @JsonField(name = "started_at")
    Date mStartedAt;
    @JsonField(name = "status")
    String mStatus;
    @JsonField(name = "tag")
    boolean mTag;
    @JsonField(name = "user")
    User mUser;

    public RepositoryCommit getCommit() {
        return mCommit;
    }

    public String getCoverage() {
        return mCoverage;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public String getArtifactsFile() {
        return mArtifactsFile;
    }

    public Date getFinishedAt() {
        return mFinishedAt;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getRef() {
        return mRef;
    }

    public Runner getRunner() {
        return mRunner;
    }

    public String getStage() {
        return mStage;
    }

    public Date getStartedAt() {
        return mStartedAt;
    }

    public String getStatus() {
        return mStatus;
    }

    public boolean isTag() {
        return mTag;
    }

    public User getUser() {
        return mUser;
    }
}
