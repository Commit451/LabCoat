package com.commit451.gitlab.navigation;

import android.net.Uri;

/**
 * Routes things. Could probably be better if it used regex. Maybe one day
 */
public class RoutingRouter {

    private RoutingNavigator mNavigator;

    public RoutingRouter(RoutingNavigator routingNavigator) {
        mNavigator = routingNavigator;
    }

    public void route(Uri link) {
        if (link.getPath().contains("issues")) {
            if (link.getLastPathSegment().equals("issues")) {
                //this means it was just a link to something like
                //gitlab.com/Commit451/LabCoat/issues
                int index = link.getPathSegments().indexOf("issues");
                if (index != -1 && index > 1) {
                    String namespace = link.getPathSegments().get(index-2);
                    String projectId = link.getPathSegments().get(index-1);
                    mNavigator.onRouteToProject(namespace, projectId);
                    return;
                }
            } else {
                int index = link.getPathSegments().indexOf("issues");
                //this is good, it means it is a link to an actual issue
                if (index != -1 && index > 1 && link.getPathSegments().size() > index) {
                    String projectNamespace = link.getPathSegments().get(index-2);
                    String projectName = link.getPathSegments().get(index-1);
                    String lastSegment = link.getPathSegments().get(index+1);
                    //We have to do this cause there can be args on the url, such as
                    //https://gitlab.com/Commit451/LabCoat/issues/158#note_4560580
                    String[] stuff = lastSegment.split("#");
                    String issueIid = stuff[0];
                    mNavigator.onRouteToIssue(projectNamespace, projectName, issueIid);
                    return;
                }
            }
        } else if (link.getPath().contains("commits")) {
            //Order matters here, parse commits first, then commit
            int index = link.getPathSegments().indexOf("commits");
            if (index > 1) {
                String projectNamespace = link.getPathSegments().get(index-2);
                String projectName = link.getPathSegments().get(index-1);
                mNavigator.onRouteToProject(projectNamespace, projectName);
                return;
            }
        } else if (link.getPath().contains("commit")) {
            int index = link.getPathSegments().indexOf("commit");
            if (index > 1 && index < link.getPathSegments().size()) {
                String projectNamespace = link.getPathSegments().get(index-2);
                String projectName = link.getPathSegments().get(index-1);
                String commitSha = link.getPathSegments().get(index+1);
                mNavigator.onRouteToCommit(projectNamespace, projectName, commitSha);
                return;
            }
        }  else if (link.getPath().contains("compare")) {
            int index = link.getPathSegments().indexOf("compare");
            if (index > 1 && index < link.getPathSegments().size()) {
                String projectNamespace = link.getPathSegments().get(index-2);
                String projectName = link.getPathSegments().get(index-1);
                //comparing two commit shas
                String[] shas = link.getLastPathSegment().split("...");
                if (shas.length == 2) {
                    //I believe we want to route to the second one. Should verify this
                    mNavigator.onRouteToCommit(projectNamespace, projectName, shas[1]);
                    return;
                }
            }
        } else if (link.getPath().contains("merge_requests")) {
            int index = link.getPathSegments().indexOf("merge_requests");
            if (index > 1 && index < link.getPathSegments().size()) {
                String projectNamespace = link.getPathSegments().get(index-2);
                String projectName = link.getPathSegments().get(index-1);
                String mergeRequestId = link.getPathSegments().get(index+1);
                mNavigator.onRouteToMergeRequest(projectNamespace, projectName, mergeRequestId);
                return;
            }
        } else if (link.getPath().contains("builds")) {
            int index = link.getPathSegments().indexOf("builds");
            if (index > 1 && index < link.getPathSegments().size()) {
                String projectNamespace = link.getPathSegments().get(index-2);
                String projectName = link.getPathSegments().get(index-1);
                String buildId = link.getPathSegments().get(index+1);
                mNavigator.onRouteToBuild(projectNamespace, projectName, buildId);
                return;
            }
        }
        mNavigator.onRouteUnknown(link);
    }
}
