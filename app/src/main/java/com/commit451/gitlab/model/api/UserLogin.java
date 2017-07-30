package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class UserLogin extends UserFull {
    @Json(name = "private_token")
    String privateToken;

    public UserLogin() {}

    public String getPrivateToken() {
        return privateToken;
    }
}
