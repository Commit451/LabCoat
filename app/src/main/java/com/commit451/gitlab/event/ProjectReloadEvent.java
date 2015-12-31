package com.commit451.gitlab.event;

import com.commit451.gitlab.model.api.Project;

/**
 * Signifies that either a project or its branch has changed and there needs to be a reload
 * Created by Jawn on 9/22/2015.
 */
public class ProjectReloadEvent {
    public final Project mProject;
    public final String mBranchName;

    public ProjectReloadEvent(Project project, String branchName) {
        this.mProject = project;
        this.mBranchName = branchName;
    }
}
