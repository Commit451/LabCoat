package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Session extends User {
    @SerializedName("private_token")
    String mPrivateToken;

    public Session() {}

    public String getPrivateToken() {
        return mPrivateToken;
    }
}
