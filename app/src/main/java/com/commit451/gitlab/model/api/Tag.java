package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * A tag in Git
 */
@JsonObject
public class Tag {

    @JsonField(name = "name")
    String mName;
    @JsonField(name = "message")
    String mMessage;
    @JsonField(name = "commit")
    Commit mCommit;
    @JsonField(name = "release")
    Release mRelease;

    protected Tag() {

    }

    public String getName() {
        return mName;
    }

    public String getMessage() {
        return mMessage;
    }

    public Commit getCommit() {
        return mCommit;
    }

    public Release getRelease() {
        return mRelease;
    }

    @JsonObject
    public static class Commit {

        @JsonField(name = "id")
        String mId;
        @JsonField(name = "message")
        String mMessage;
    }

    @JsonObject
    public static class Release {
        @JsonField(name = "tag_name")
        String mTagName;
        @JsonField(name = "description")
        String mDescription;
    }
}
