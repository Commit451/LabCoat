package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.util.List;

@Parcel
@JsonObject
public class GroupDetail extends Group {
    @JsonField(name = "projects")
    List<Project> projects;

    public GroupDetail() {}

    public List<Project> getProjects() {
        return projects;
    }
}
