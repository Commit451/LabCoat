package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

/**
 * Represents a build
 */
@Parcel
public class Build {

    @SerializedName("commit")
    RepositoryCommit mCommit;
    @SerializedName("coverage")
    String mCoverage;
    @SerializedName("created_at")
    Date mCreatedAt;
    @SerializedName("artifacts_file")
    String mArtifactsFile;
    @SerializedName("finished_at")
    Date mFinishedAt;
    @SerializedName("id")
    long mId;
    @SerializedName("name")
    String mName;
    @SerializedName("ref")
    String mRef;
    @SerializedName("runner")
    Runner mRunner;
    @SerializedName("stage")
    String mStage;
    @SerializedName("started_at")
    Date mStartedAt;
    @SerializedName("status")
    String mStatus;
    @SerializedName("tag")
    boolean mTag;
    @SerializedName("user")
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
