package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.commit451.gitlab.util.ObjectUtil;

import org.parceler.Parcel;

@Parcel
@JsonObject
public class Branch {
    @JsonField(name = "name")
    String name;
    @JsonField(name = "protected")
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
        return ObjectUtil.equals(name, branch.name);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
