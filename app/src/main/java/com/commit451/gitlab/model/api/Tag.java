package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * A tag in Git
 */
@JsonObject
public class Tag {

    @JsonField(name = "name")
    String name;
    @JsonField(name = "message")
    String message;
    @JsonField(name = "commit")
    Commit commit;
    @JsonField(name = "release")
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
