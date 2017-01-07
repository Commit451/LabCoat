package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.util.Date;

/**
 * A snippet!
 */
@Parcel
@JsonObject
public class Snippet {
    @JsonField(name = "id")
    long id;
    @JsonField(name = "title")
    String title;
    @JsonField(name = "file_name")
    String fileName;
    @JsonField(name = "author")
    Author author;
    @JsonField(name = "expires_at")
    Date expiresAt;
    @JsonField(name = "updated_at")
    Date updatedAt;
    @JsonField(name = "created_at")
    Date createdAt;

    protected Snippet() {
        //for json
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    @Parcel
    @JsonObject
    public static class Author {
        @JsonField(name = "id")
        long mId;
        @JsonField(name = "username")
        String mUsername;
        @JsonField(name = "email")
        String mEmail;
        @JsonField(name = "name")
        String mName;
        @JsonField(name = "state")
        String mState;
        @JsonField(name = "created_at")
        Date mCreatedAt;

        protected Author() {
            //for json
        }
    }
}
