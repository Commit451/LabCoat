package com.commit451.gitlab;

import android.net.Uri;

import com.commit451.gitlab.navigation.RoutingNavigator;

/**
 * Checks counting
 */
public class CountingRouter implements RoutingNavigator {

    public int issueRouteCount = 0;
    public int commitRouteCount = 0;
    public int mergeRequestRouteCount = 0;
    public int projectRouteCount = 0;
    public int buildRoutCount = 0;
    public int unknownRountCount = 0;

    @Override
    public void onRouteToIssue(String projectNamespace, String projectName, String issueIid) {
        issueRouteCount++;
    }

    @Override
    public void onRouteToCommit(String projectNamespace, String projectName, String commitSha) {
        commitRouteCount++;
    }

    @Override
    public void onRouteToMergeRequest(String projectNamespace, String projectName, String mergeRequestId) {
        mergeRequestRouteCount++;
    }

    @Override
    public void onRouteToProject(String namespace, String projectId) {
        projectRouteCount++;
    }

    @Override
    public void onRouteToBuild(String projectNamespace, String projectName, String buildNumber) {
        buildRoutCount++;
    }

    @Override
    public void onRouteUnknown(Uri uri) {
        unknownRountCount++;
    }
}
