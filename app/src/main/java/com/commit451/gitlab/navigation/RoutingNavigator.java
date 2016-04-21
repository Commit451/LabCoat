package com.commit451.gitlab.navigation;

import android.net.Uri;

/**
 * Interface called when routing in the Routing activity
 */
public interface RoutingNavigator {
    void onRouteToIssue(String projectNamespace, String projectName, String issueIid);
    void onRouteToCommit(String projectNamespace, String projectName, String commitSha);
    void onRouteToMergeRequest(String projectNamespace, String projectName, String mergeRequestId);
    void onRouteToProject(String namespace, String projectId);
    void onRouteUnknown(Uri uri);
}
