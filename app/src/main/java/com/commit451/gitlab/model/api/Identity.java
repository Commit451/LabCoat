package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class Identity {
    @Json(name = "provider")
    String provider;
    @Json(name = "extern_uid")
    String externUid;

    public Identity() {}

    public String getProvider() {
        return provider;
    }

    public String getExternUid() {
        return externUid;
    }
}
