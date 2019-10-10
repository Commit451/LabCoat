package com.commit451.gitlab.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri

import com.commit451.gitlab.R
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Generates deeplinks
 */
object DeepLinker {

    const val KEY_ORIGINAL_URL = "original_url"

    fun generateDeeplinkIntentFromUri(context: Context, originalUri: Uri): Intent {
        val uri = originalUri.buildUpon()
                .scheme(context.getString(R.string.deeplink_scheme))
                .build()
        return generatePrivateIntent(context, uri, originalUri)
    }

    fun route(url: String?, navigator: Callbacks) {
        if (url == null) {
            navigator.onRouteUnknown(null)
            return
        }
        // It doesn't like it if we have a host like this, so replace it
        val link = url.replaceFirst("labcoat://", "https://").toHttpUrlOrNull()
        if (link == null) {
            navigator.onRouteUnknown(url)
            return
        }
        val path = link.pathSegments.joinToString("/", "/")
        if (path.contains("issues")) {
            if (link.pathSegments.last() == "issues") {
                //this means it was just a link to something like
                //gitlab.com/Commit451/LabCoat/issues
                val index = link.pathSegments.indexOf("issues")
                if (index != -1 && index > 1) {
                    val namespace = link.pathSegments[index - 2]
                    val name = link.pathSegments[index - 1]
                    //TODO make this tell what tab to open up when we get to projects
                    navigator.onRouteToProject(namespace, name, ProjectSelection.ISSUES)
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
        } else if (path.contains("commits")) {
            //Order matters here, parse commits first, then commit
            val index = link.pathSegments.indexOf("commits")
            if (index > 1) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                navigator.onRouteToProject(projectNamespace, projectName, ProjectSelection.COMMITS)
                return
            }
        } else if (path.contains("commit")) {
            val index = link.pathSegments.indexOf("commit")
            if (index > 1 && index < link.pathSegments.size) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                val commitSha = link.pathSegments[index + 1]
                navigator.onRouteToCommit(projectNamespace, projectName, commitSha)
                return
            }
        } else if (path.contains("compare")) {
            val index = link.pathSegments.indexOf("compare")
            if (index > 1 && index < link.pathSegments.size) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                //comparing two commit shas
                val shas = link.pathSegments.last().split("...".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (shas.size == 2) {
                    //I believe we want to route to the second one. Should verify this
                    navigator.onRouteToCommit(projectNamespace, projectName, shas[1])
                    return
                }
            }
        } else if (path.contains("merge_requests")) {
            val index = link.pathSegments.indexOf("merge_requests")
            if (index > 1 && index < link.pathSegments.size) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                val mergeRequestId = link.pathSegments[index + 1]
                navigator.onRouteToMergeRequest(projectNamespace, projectName, mergeRequestId)
                return
            }
        } else if (path.contains("builds")) {
            val index = link.pathSegments.indexOf("builds")
            if (index > 1 && index < link.pathSegments.size) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                val buildId = link.pathSegments[index + 1]
                navigator.onRouteToBuild(projectNamespace, projectName, buildId)
                return
            }
        } else if (path.contains("milestones")) {
            val index = link.pathSegments.indexOf("milestones")
            if (index > 1 && index < link.pathSegments.size) {
                val projectNamespace = link.pathSegments[index - 2]
                val projectName = link.pathSegments[index - 1]
                val milestoneId = link.pathSegments[index + 1]
                navigator.onRouteToMilestone(projectNamespace, projectName, milestoneId)
                return
            }
        } else if (link.pathSegments.size == 2) {
            //exactly two path segments, it is probably a link to a project
            val projectNamespace = link.pathSegments[0]
            val projectName = link.pathSegments[1]
            navigator.onRouteToProject(projectNamespace, projectName, ProjectSelection.PROJECT)
            return
        }
        navigator.onRouteUnknown(url)
    }

    private fun generatePrivateIntent(context: Context, uri: Uri, originalUri: Uri): Intent {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.putExtra(KEY_ORIGINAL_URL, originalUri)
        intent.`package` = context.packageName
        return intent
    }

    /**
     * Interface called when routing in the Routing activity
     */
    interface Callbacks {
        fun onRouteToIssue(projectNamespace: String, projectName: String, issueIid: String)
        fun onRouteToCommit(projectNamespace: String, projectName: String, commitSha: String)
        fun onRouteToMergeRequest(projectNamespace: String, projectName: String, mergeRequestIid: String)
        fun onRouteToProject(projectNamespace: String, projectName: String, selection: ProjectSelection)
        fun onRouteToBuild(projectNamespace: String, projectName: String, buildNumber: String)
        fun onRouteToMilestone(projectNamespace: String, projectName: String, milestoneNumber: String)
        fun onRouteUnknown(url: String?)
    }

    enum class ProjectSelection {
        PROJECT,
        ISSUES,
        COMMITS,
        MILESTONES,
        JOBS,
        MERGE_REQUESTS
    }
}
