package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.commit451.gitlab.util.ObjectUtil;

import org.parceler.Parcel;

@Parcel
@JsonObject
public class Branch {
    @JsonField(name = "name")
    String mName;
    @JsonField(name = "protected")
    boolean mProtected;

    public Branch() {}

    public String getName() {
        return mName;
    }

    public boolean isProtected() {
        return mProtected;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Branch)) {
            return false;
        }

        Branch branch = (Branch) o;
        return ObjectUtil.equals(mName, branch.mName);
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hash(mName);
    }

    @Override
    public String toString() {
        return mName;
    }
}
