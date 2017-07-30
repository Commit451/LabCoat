package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class ForkedFromProject {
    @Json(name = "id")
    long id;
    @Json(name = "name")
    String name;
    @Json(name = "name_with_namespace")
    String nameWithNamespace;
    @Json(name = "path")
    String path;
    @Json(name = "path_with_namespace")
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
