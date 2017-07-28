package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class GroupDetail extends Group {
    @Json(name = "projects")
    List<Project> projects;

    public GroupDetail() {}

    public List<Project> getProjects() {
        return projects;
    }
}
