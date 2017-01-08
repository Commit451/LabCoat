package com.commit451.gitlab.event;

import com.commit451.gitlab.model.api.Project;

/**
 * Signifies that either a project or its branch has changed and there needs to be a reload
 */
public class ProjectReloadEvent {
    public final Project project;
    public final String branchName;

    public ProjectReloadEvent(Project project, String branchName) {
        this.project = project;
        this.branchName = branchName;
    }
}
