package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

@Parcel
@JsonObject
public class UserLogin extends UserFull {
    @JsonField(name = "private_token")
    String privateToken;

    public UserLogin() {}

    public String getPrivateToken() {
        return privateToken;
    }
}
