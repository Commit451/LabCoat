package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

@Parcel
@JsonObject
public class ForkedFromProject {
    @JsonField(name = "id")
    long id;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "name_with_namespace")
    String nameWithNamespace;
    @JsonField(name = "path")
    String path;
    @JsonField(name = "path_with_namespace")
    String pathWithNamespace;

    public ForkedFromProject() {}

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNameWithNamespace() {
        return nameWithNamespace;
    }

    public String getPath() {
        return path;
    }

    public String getPathWithNamespace() {
        return pathWithNamespace;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ForkedFromProject)) {
            return false;
        }

        ForkedFromProject that = (ForkedFromProject) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
