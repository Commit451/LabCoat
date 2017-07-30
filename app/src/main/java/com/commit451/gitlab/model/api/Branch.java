package com.commit451.gitlab.model.api;

import com.commit451.gitlab.util.ObjectUtil;
import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class Branch {
    @Json(name = "name")
    String name;
    @Json(name = "protected")
    boolean isProtected;

    public Branch() {}

    public String getName() {
        return name;
    }

    public boolean isProtected() {
        return isProtected;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Branch)) {
            return false;
        }

        Branch branch = (Branch) o;
        return ObjectUtil.INSTANCE.equals(name, branch.name);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.INSTANCE.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
