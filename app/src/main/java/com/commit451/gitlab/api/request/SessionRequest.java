package com.commit451.gitlab.api.request;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Start a session
 */
@JsonObject
public class SessionRequest {

    @JsonField(name = "email")
    String email;
    @JsonField(name = "login")
    String login;
    @JsonField(name = "password")
    String password;

    public SessionRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public SessionRequest setLogin(String login) {
        this.login = login;
        return this;
    }

    public SessionRequest setPassword(String password) {
        this.password = password;
        return this;
    }
}
