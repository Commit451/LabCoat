package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

@Parcel
@JsonObject
public class Email {
    @JsonField(name = "id")
    long mId;
    @JsonField(name = "email")
    String mEmail;

    public Email() {}

    public long getId() {
        return mId;
    }

    public String getEmail() {
        return mEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Email)) {
            return false;
        }

        Email email = (Email) o;
        return mId == email.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }
}
