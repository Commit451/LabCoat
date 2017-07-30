package com.commit451.gitlab

import android.net.Uri
import com.commit451.gitlab.navigation.RoutingNavigator

/**
 * Checks counting
 */
class CountingRouter : RoutingNavigator {

    var issueRouteCount = 0
    var commitRouteCount = 0
    var mergeRequestRouteCount = 0
    var projectRouteCount = 0
    var buildRouteCount = 0
    var milestoneRouteCount: Int = 0
    var unknownRountCount = 0

    override fun onRouteToIssue(projectNamespace: String, projectName: String, issueIid: String) {
        issueRouteCount++
    }

    override fun onRouteToCommit(projectNamespace: String, projectName: String, commitSha: String) {
        commitRouteCount++
    }

    override fun onRouteToMergeRequest(projectNamespace: String, projectName: String, mergeRequestId: String) {
        mergeRequestRouteCount++
    }

    override fun onRouteToProject(namespace: String, projectId: String) {
        projectRouteCount++
    }

    override fun onRouteToBuild(projectNamespace: String, projectName: String, buildNumber: String) {
        buildRouteCount++
    }

    override fun onRouteToMilestone(projectNamespace: String, projectName: String, milestoneNumber: String) {
        milestoneRouteCount++
    }

    override fun onRouteUnknown(uri: Uri?) {
        unknownRountCount++
    }
}
