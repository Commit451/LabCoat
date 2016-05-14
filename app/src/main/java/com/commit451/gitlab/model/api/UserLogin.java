package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

@Parcel
public class UserLogin extends UserFull {
    @JsonField(name = "private_token")
    String mPrivateToken;

    public UserLogin() {}

    public String getPrivateToken() {
        return mPrivateToken;
    }
}
