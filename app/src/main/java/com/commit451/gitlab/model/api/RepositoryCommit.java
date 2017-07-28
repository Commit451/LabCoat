package com.commit451.gitlab.model.api;

import com.commit451.gitlab.util.ObjectUtil;
import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class RepositoryCommit {
    @Json(name = "id")
    String id;
    @Json(name = "short_id")
    String shortId;
    @Json(name = "title")
    String title;
    @Json(name = "author_name")
    String authorName;
    @Json(name = "author_email")
    String authorEmail;
    @Json(name = "created_at")
    Date createdAt;
    @Json(name = "message")
    String message;

    public RepositoryCommit() {}

    public String getId() {
        return id;
    }

    public String getShortId() {
        return shortId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RepositoryCommit)) {
            return false;
        }

        RepositoryCommit commit = (RepositoryCommit) o;
        return ObjectUtil.INSTANCE.equals(id, commit.id);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.INSTANCE.hash(id);
    }
}
