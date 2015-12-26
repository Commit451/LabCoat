package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Identity {
    @SerializedName("provider")
    String mProvider;
    @SerializedName("extern_uid")
    String mExternUid;

    public Identity() {}

    public String getProvider() {
        return mProvider;
    }

    public String getExternUid() {
        return mExternUid;
    }
}
