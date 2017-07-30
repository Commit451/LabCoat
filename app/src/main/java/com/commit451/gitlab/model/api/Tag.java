package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

/**
 * A tag in Git
 */
public class Tag {

    @Json(name = "name")
    String name;
    @Json(name = "message")
    String message;
    @Json(name = "commit")
    Commit commit;
    @Json(name = "release")
    Release release;

    protected Tag() {

    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public Commit getCommit() {
        return commit;
    }

    public Release getRelease() {
        return release;
    }

    public static class Commit {

        @Json(name = "id")
        String mId;
        @Json(name = "message")
        String mMessage;
    }

    public static class Release {
        @Json(name = "tag_name")
        String mTagName;
        @Json(name = "description")
        String mDescription;
    }
}
