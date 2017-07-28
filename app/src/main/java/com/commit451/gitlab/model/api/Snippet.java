package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.Date;

/**
 * A snippet!
 */
@Parcel
public class Snippet {
    @Json(name = "id")
    long id;
    @Json(name = "title")
    String title;
    @Json(name = "file_name")
    String fileName;
    @Json(name = "author")
    Author author;
    @Json(name = "expires_at")
    Date expiresAt;
    @Json(name = "updated_at")
    Date updatedAt;
    @Json(name = "created_at")
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
    public static class Author {
        @Json(name = "id")
        long mId;
        @Json(name = "username")
        String mUsername;
        @Json(name = "email")
        String mEmail;
        @Json(name = "name")
        String mName;
        @Json(name = "state")
        String mState;
        @Json(name = "created_at")
        Date mCreatedAt;

        protected Author() {
            //for json
        }
    }
}
