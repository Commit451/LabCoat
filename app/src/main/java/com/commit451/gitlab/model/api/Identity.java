package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

@Parcel
public class Identity {
    @JsonField(name = "provider")
    String mProvider;
    @JsonField(name = "extern_uid")
    String mExternUid;

    public Identity() {}

    public String getProvider() {
        return mProvider;
    }

    public String getExternUid() {
        return mExternUid;
    }
}
