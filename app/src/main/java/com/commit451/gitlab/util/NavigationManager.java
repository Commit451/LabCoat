package com.commit451.gitlab.util;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.ImageView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.AboutActivity;
import com.commit451.gitlab.activity.AddIssueActivity;
import com.commit451.gitlab.activity.AddMilestoneActivity;
import com.commit451.gitlab.activity.AddUserActivity;
import com.commit451.gitlab.activity.FileActivity;
import com.commit451.gitlab.activity.GroupActivity;
import com.commit451.gitlab.activity.GroupsActivity;
import com.commit451.gitlab.activity.IssueActivity;
import com.commit451.gitlab.activity.LoginActivity;
import com.commit451.gitlab.activity.MergeRequestActivity;
import com.commit451.gitlab.activity.MilestoneActivity;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.activity.ProjectsActivity;
import com.commit451.gitlab.activity.SearchActivity;
import com.commit451.gitlab.activity.SettingsActivity;
import com.commit451.gitlab.activity.UserActivity;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.UserBasic;

import java.util.List;

import timber.log.Timber;

/**
 * Manages navigation so that we can override things as needed
 * Created by Jawn on 9/21/2015.
 */
public class NavigationManager {

    public static void navigateToAbout(Activity activity) {
        activity.startActivity(AboutActivity.newInstance(activity));
    }

    public static void navigateToSettings(Activity activity) {
        activity.startActivity(SettingsActivity.newInstance(activity));
    }

    public static void navigateToProject(Activity activity, Project project) {
        activity.startActivity(ProjectActivity.newInstance(activity, project));
    }

    public static void navigateToProject(Activity activity, long projectId) {
        activity.startActivity(ProjectActivity.newInstance(activity, projectId));
    }

    public static void navigateToProjects(Activity activity) {
        activity.startActivity(ProjectsActivity.newInstance(activity));
    }

    public static void navigateToGroups(Activity activity) {
        activity.startActivity(GroupsActivity.newInstance(activity));
    }

    public static void navigateToLogin(Activity activity) {
        activity.startActivity(LoginActivity.newInstance(activity));
    }

    public static void navigateToSearch(Activity activity) {
        activity.startActivity(SearchActivity.newInstance(activity));
    }

    public static void navigateToUser(Activity activity, UserBasic user) {
        navigateToUser(activity, null, user);
    }

    public static void navigateToUser(Activity activity, ImageView profileImage, UserBasic user) {
        Intent intent = UserActivity.newInstance(activity, user);
        if (Build.VERSION.SDK_INT >= 21 && profileImage != null) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(activity, profileImage, activity.getString(R.string.transition_user));
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    public static void navigateToGroup(Activity activity, ImageView profileImage, Group group) {
        Intent intent = GroupActivity.newInstance(activity, group);
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(activity, profileImage, activity.getString(R.string.transition_user));
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    public static void navigateToGroup(Activity activity, long groupId) {
        activity.startActivity(GroupActivity.newInstance(activity, groupId));
    }

    public static void navigateToMilestone(Activity activity, Project project, Milestone milestone) {
        activity.startActivity(MilestoneActivity.newInstance(activity, project, milestone));
    }

    public static void navigateToIssue(Activity activity, Project project, Issue issue) {
        activity.startActivity(IssueActivity.newInstance(activity, project, issue));
    }

    public static void navigateToMergeRequest(Activity activity, Project project, MergeRequest mergeRequest) {
        Intent intent = MergeRequestActivity.newInstance(activity, project, mergeRequest);
        activity.startActivity(intent);
    }

    public static void navigateToFile(Activity activity, long projectId, String path, String branchName) {
        activity.startActivity(FileActivity.newIntent(activity, projectId, path, branchName));
    }

    public static void navigateToAddProjectMember(Activity activity, View fab, long projectId) {
        Intent intent = AddUserActivity.newIntent(activity, projectId);
        startMorphActivity(activity, fab, intent);
    }

    public static void navigateToAddGroupMember(Activity activity, View fab, Group group) {
        Intent intent = AddUserActivity.newIntent(activity, group);
        startMorphActivity(activity, fab, intent);
    }

    public static void navigateToEditIssue(Activity activity, View fab, Project project, Issue issue) {
        navigateToAddIssue(activity, fab, project, issue);
    }

    public static void navigateToAddIssue(Activity activity, View fab, Project project) {
        navigateToAddIssue(activity, fab, project, null);
    }

    private static void navigateToAddIssue(Activity activity, View fab, Project project, Issue issue) {
        Intent intent = AddIssueActivity.newIntent(activity, project, issue);
        startMorphActivity(activity, fab, intent);
    }

    public static void navigateToAddMilestone(Activity activity, View fab, Project project) {
        Intent intent = AddMilestoneActivity.newInstance(activity, project.getId());
        startMorphActivity(activity, fab, intent);
    }

    public static void navigateToEditMilestone(Activity activity, View fab, Project project, Milestone milestone) {
        Intent intent = AddMilestoneActivity.newInstance(activity, project.getId(), milestone);
        startMorphActivity(activity, fab, intent);
    }

    private static void startMorphActivity(Activity activity, View fab, Intent intent) {
        if (Build.VERSION.SDK_INT >= 21 && fab != null) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
                    (activity, fab, activity.getString(R.string.transition_morph));
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
        }
    }

    public static void navigateToUrl(Activity activity, Uri uri, Account account) {
        Timber.d("navigateToUrl: %s", uri);
        if (account.getServerUrl().getHost().equals(uri.getHost())) {
            boolean handled = navigateToUrl(activity, uri);
            if (!handled) {
                IntentUtil.openPage(activity, uri.toString());
            }
        } else {
            IntentUtil.openPage(activity, uri.toString());
        }
    }

    /**
     * Attempts to map a url to an activity within the app
     * @param activity the current activity
     * @param uri the url we want to map
     * @return true if we navigated somewhere, false otherwise
     */
    private static boolean navigateToUrl(Activity activity, Uri uri) {
        //TODO figure out the url to activity mapping
        if (uri.getPath().contains("issues")) {
            List<String> pathSegments = uri.getPathSegments();
            for (int i=0; i<pathSegments.size(); i++) {
                //segment == issues, and there is one more segment in the path
                if (pathSegments.get(i).equals("issues") && i != pathSegments.size()-1) {
                    //TODO this would probably break if we had query params or anything else in the url
                    String issueId = pathSegments.get(i+1);
                    //TODO actually navigate to issue activity which will load the needed project and issue
                    //navigateToIssue(activity, null, issueId);
                    return true;
                }
            }
            navigateToProject(activity, -1);
            return true;
        }
        return false;
    }
}
