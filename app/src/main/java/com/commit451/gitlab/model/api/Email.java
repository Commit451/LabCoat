package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class Email {
    @Json(name = "id")
    long id;
    @Json(name = "email")
    String email;

    public Email() {}

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Email)) {
            return false;
        }

        Email email = (Email) o;
        return id == email.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
