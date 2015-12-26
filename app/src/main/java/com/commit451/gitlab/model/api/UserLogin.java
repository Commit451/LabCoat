package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class UserLogin extends UserFull {
    @SerializedName("private_token")
    String mPrivateToken;

    public UserLogin() {}

    public String getPrivateToken() {
        return mPrivateToken;
    }
}
