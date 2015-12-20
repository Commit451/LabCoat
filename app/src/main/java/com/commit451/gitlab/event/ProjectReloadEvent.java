package com.commit451.gitlab.event;

import com.commit451.gitlab.model.Project;

/**
 * Signifies that either a project or its branch has changed and there needs to be a reaload
 * Created by Jawn on 9/22/2015.
 */
public class ProjectReloadEvent {
    public Project project;
    public String branchName;

    public ProjectReloadEvent(Project project, String branchName) {
        this.project = project;
        this.branchName = branchName;
    }
}
