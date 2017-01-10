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
    String id;
    @JsonField(name = "short_id")
    String shortId;
    @JsonField(name = "title")
    String title;
    @JsonField(name = "author_name")
    String authorName;
    @JsonField(name = "author_email")
    String authorEmail;
    @JsonField(name = "created_at")
    Date createdAt;
    @JsonField(name = "message")
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
