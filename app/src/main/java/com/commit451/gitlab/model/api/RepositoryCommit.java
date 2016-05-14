package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.commit451.gitlab.util.ObjectUtil;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
@JsonObject
public class RepositoryCommit {
    @JsonField(name = "id")
    String mId;
    @JsonField(name = "short_id")
    String mShortId;
    @JsonField(name = "title")
    String mTitle;
    @JsonField(name = "author_name")
    String mAuthorName;
    @JsonField(name = "author_email")
    String mAuthorEmail;
    @JsonField(name = "created_at")
    Date mCreatedAt;
    @JsonField(name = "message")
    String mMessage;

    public RepositoryCommit() {}

    public String getId() {
        return mId;
    }

    public String getShortId() {
        return mShortId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public String getAuthorEmail() {
        return mAuthorEmail;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public String getMessage() {
        return mMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RepositoryCommit)) {
            return false;
        }

        RepositoryCommit commit = (RepositoryCommit) o;
        return ObjectUtil.equals(mId, commit.mId);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hash(mId);
    }
}
