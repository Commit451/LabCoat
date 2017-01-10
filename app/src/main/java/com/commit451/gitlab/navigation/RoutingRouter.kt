package com.commit451.gitlab.navigation

import android.net.Uri

/**
 * Routes things. Could probably be better if it used regex. Maybe one day
 */
class RoutingRouter(private val navigator: RoutingNavigator) {

    fun route(link: Uri?) {
        if (link == null) {
            navigator.onRouteUnknown(null)
            return
        }
        if (link.path == null) {
            navigator.onRouteUnknown(link)
            return
        }
        if (link.path.contains("issues")) {
            if (link.lastPathSegment == "issues") {
                //this means it was just a link to something like
                //gitlab.com/Commit451/LabCoat/issues
                val index = link.pathSegments.indexOf("issues")
                if (index != -1 && index > 1) {
                    val namespace = link.pathSegments[index - 2]
                    val name = link.pathSegments[index - 1]
                    //TODO make this tell what tab to open up when we get to projects
                    navigator.onRouteToProject(namespace, name)
                    return
                }
            } else {
                val index = link.pathSegments.indexOf("issues")
                //this is good, it means it is a link to an actual issue
                if (index != -1 && index > 1 && link.pathSegments.size > index) {
                    val projectNamespace = link.pathSegments[index - 2]
                    val projectName = link.pathSegments[index - 1]
                    val lastSegment = link.pathSegments[index + 1]
                    //We have to do this cause there can be args on the url, such as
                    //https://gitlab.com/Commit451/LabCoat/issues/158#note_4560580
                    val stuff = lastSegment.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val issueIid = stuff[0]
                    navigator.onRouteToIssue(projectNamespace, projectName, issueIid)
                    return
                }
            }
        } else if (link.path.contains("commits")) {
            //Order matters here, parse commits first, then commit
            val index = link.pathSegments.indexOf("commits")
            if (index > 1) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                navigator.onRouteToProject(projectNamespace, projectName)
                return
            }
        } else if (link.path.contains("commit")) {
            val index = link.pathSegments.indexOf("commit")
            if (index > 1 && index < link.pathSegments.size) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                val commitSha = link.pathSegments[index + 1]
                navigator.onRouteToCommit(projectNamespace, projectName, commitSha)
                return
            }
        } else if (link.path.contains("compare")) {
            val index = link.pathSegments.indexOf("compare")
            if (index > 1 && index < link.pathSegments.size) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                //comparing two commit shas
                val shas = link.lastPathSegment.split("...".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (shas.size == 2) {
                    //I believe we want to route to the second one. Should verify this
                    navigator.onRouteToCommit(projectNamespace, projectName, shas[1])
                    return
                }
            }
        } else if (link.path.contains("merge_requests")) {
            val index = link.pathSegments.indexOf("merge_requests")
            if (index > 1 && index < link.pathSegments.size) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                val mergeRequestId = link.pathSegments[index + 1]
                navigator.onRouteToMergeRequest(projectNamespace, projectName, mergeRequestId)
                return
            }
        } else if (link.path.contains("builds")) {
            val index = link.pathSegments.indexOf("builds")
            if (index > 1 && index < link.pathSegments.size) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                val buildId = link.pathSegments[index + 1]
                navigator.onRouteToBuild(projectNamespace, projectName, buildId)
                return
            }
        } else if (link.path.contains("milestones")) {
            val index = link.pathSegments.indexOf("milestones")
            if (index > 1 && index < link.pathSegments.size) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                val milestoneId = link.pathSegments[index + 1]
                navigator.onRouteToMilestone(projectNamespace, projectName, milestoneId)
                return
            }
        }
        navigator.onRouteUnknown(link)
    }
}
