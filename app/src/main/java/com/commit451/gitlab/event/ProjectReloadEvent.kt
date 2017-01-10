package com.commit451.gitlab.event

import com.commit451.gitlab.model.api.Project

/**
 * Signifies that either a project or its branch has changed and there needs to be a reload
 */
class ProjectReloadEvent(val project: Project, val branchName: String)
