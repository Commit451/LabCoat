package com.commit451.gitlab.model.api;


import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * The structure for a message from the server, which is usually found in a response body
 */
@JsonObject
public class Message {

    @JsonField(name = "message")
    String message;

    public String getMessage() {
        return message;
    }
}
