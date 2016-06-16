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
    long mId;
    @JsonField(name = "title")
    String mTitle;
    @JsonField(name = "file_name")
    String mFileName;
    @JsonField(name = "author")
    Author mAuthor;
    @JsonField(name = "expires_at")
    Date mExpiresAt;
    @JsonField(name = "updated_at")
    Date mUpdatedAt;
    @JsonField(name = "created_at")
    Date mCreatedAt;

    protected Snippet() {
        //for json
    }

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getFileName() {
        return mFileName;
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
