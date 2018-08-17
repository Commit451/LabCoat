package com.commit451.gitlab

import com.commit451.gitlab.navigation.DeepLinker

class CounterCallbacks: DeepLinker.Callbacks {

    var issue = 0
    var commit = 0
    var mergeRequest = 0
    var project = 0
    var build = 0
    var milestone = 0
    var unknown = 0

    override fun onRouteToIssue(projectNamespace: String, projectName: String, issueIid: String) {
        issue++
    }

    override fun onRouteToCommit(projectNamespace: String, projectName: String, commitSha: String) {
        commit++
    }

    override fun onRouteToMergeRequest(projectNamespace: String, projectName: String, mergeRequestIid: String) {
        mergeRequest++
    }

    override fun onRouteToProject(projectNamespace: String, projectName: String, selection: DeepLinker.ProjectSelection) {
        project++
    }

    override fun onRouteToBuild(projectNamespace: String, projectName: String, buildNumber: String) {
        build++
    }

    override fun onRouteToMilestone(projectNamespace: String, projectName: String, milestoneNumber: String) {
        milestone++
    }

    override fun onRouteUnknown(url: String?) {
        unknown++
    }
}