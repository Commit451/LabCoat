package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

@Parcel
@JsonObject
public class Identity {
    @JsonField(name = "provider")
    String provider;
    @JsonField(name = "extern_uid")
    String externUid;

    public Identity() {}

    public String getProvider() {
        return provider;
    }

    public String getExternUid() {
        return externUid;
    }
}
