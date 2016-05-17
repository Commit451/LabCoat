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
    public int buildRouteCount = 0;
    public int milestoneRouteCount;
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
        buildRouteCount++;
    }

    @Override
    public void onRouteToMilestone(String projectNamespace, String projectName, String milestoneNumber) {
        milestoneRouteCount++;
    }

    @Override
    public void onRouteUnknown(Uri uri) {
        unknownRountCount++;
    }
}
