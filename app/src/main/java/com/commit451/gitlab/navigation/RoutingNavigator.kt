package com.commit451.gitlab.navigation

import android.net.Uri

/**
 * Interface called when routing in the Routing activity
 */
interface RoutingNavigator {
    fun onRouteToIssue(projectNamespace: String, projectName: String, issueIid: String)
    fun onRouteToCommit(projectNamespace: String, projectName: String, commitSha: String)
    fun onRouteToMergeRequest(projectNamespace: String, projectName: String, mergeRequestIid: String)
    fun onRouteToProject(projectNamespace: String, projectName: String)
    fun onRouteToBuild(projectNamespace: String, projectName: String, buildNumber: String)
    fun onRouteToMilestone(projectNamespace: String, projectName: String, milestoneNumber: String)
    fun onRouteUnknown(uri: Uri?)
}
