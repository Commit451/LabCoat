package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class GroupDetail extends Group {
    @JsonField(name = "projects")
    List<Project> mProjects;

    public GroupDetail() {}

    public List<Project> getProjects() {
        return mProjects;
    }
}
