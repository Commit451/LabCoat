package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

@Parcel
public class ForkedFromProject {
    @JsonField(name = "id")
    long mId;
    @JsonField(name = "name")
    String mName;
    @JsonField(name = "name_with_namespace")
    String mNameWithNamespace;
    @JsonField(name = "path")
    String mPath;
    @JsonField(name = "path_with_namespace")
    String mPathWithNamespace;

    public ForkedFromProject() {}

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getNameWithNamespace() {
        return mNameWithNamespace;
    }

    public String getPath() {
        return mPath;
    }

    public String getPathWithNamespace() {
        return mPathWithNamespace;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ForkedFromProject)) {
            return false;
        }

        ForkedFromProject that = (ForkedFromProject) o;
        return mId == that.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }
}
