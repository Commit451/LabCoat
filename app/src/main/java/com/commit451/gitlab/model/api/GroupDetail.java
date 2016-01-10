package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class GroupDetail extends Group {
    @SerializedName("projects")
    List<Project> mProjects;

    public GroupDetail() {}

    public List<Project> getProjects() {
        return mProjects;
    }
}
