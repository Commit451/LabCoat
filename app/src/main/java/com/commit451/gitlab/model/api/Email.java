package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Email {
    @SerializedName("id")
    long mId;
    @SerializedName("email")
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
