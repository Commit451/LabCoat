package com.commit451.gitlab.tools;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.ImageView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.activities.AboutActivity;
import com.commit451.gitlab.activities.AddUserActivity;
import com.commit451.gitlab.activities.FileActivity;
import com.commit451.gitlab.activities.GroupActivity;
import com.commit451.gitlab.activities.GroupsActivity;
import com.commit451.gitlab.activities.IssueActivity;
import com.commit451.gitlab.activities.LoginActivity;
import com.commit451.gitlab.activities.MergeRequestActivity;
import com.commit451.gitlab.activities.ProjectActivity;
import com.commit451.gitlab.activities.ProjectsActivity;
import com.commit451.gitlab.activities.SearchActivity;
import com.commit451.gitlab.activities.UserActivity;
import com.commit451.gitlab.dialogs.NewIssuePopupDialog;
import com.commit451.gitlab.model.Group;
import com.commit451.gitlab.model.Issue;
import com.commit451.gitlab.model.MergeRequest;
import com.commit451.gitlab.model.Project;
import com.commit451.gitlab.model.User;

/**
 * Manages navigation so that we can override things as needed
 * Created by Jawn on 9/21/2015.
 */
public class NavigationManager {

    public static void navigateToAbout(Activity activity) {
        activity.startActivity(AboutActivity.newInstance(activity));
    }

    public static void navigateToProject(Activity activity, Project project) {
        activity.startActivity(ProjectActivity.newInstance(activity, project));
    }

    public static void navigateToProjects(Activity activity) {
        activity.startActivity(ProjectsActivity.newInstance(activity));
    }

    public static void navigateToGroups(Activity activity) {
        activity.startActivity(GroupsActivity.newInstance(activity));
    }

    public static void navigateToLogin(Context context) {
        context.startActivity(LoginActivity.newInstance(context));
    }

    public static void navigateToSearch(Activity activity) {
        activity.startActivity(SearchActivity.newInstance(activity));
    }

    public static void navigateToUser(Activity activity, ImageView profileImage, User user) {
        Intent intent = UserActivity.newInstance(activity, user);
        if (Build.VERSION.SDK_INT >= 21) {
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

    public static void navigateToAddProjectMember(Activity activity, long projectId) {
        activity.startActivity(AddUserActivity.newInstance(activity, projectId));
    }

    public static void navigateToAddGroupMember(Activity activity, Group group) {
        activity.startActivity(AddUserActivity.newIntent(activity, group));
    }

    public static void navigateToEditIssue(Activity activity, Project project, Issue issue) {
        navigateToAddIssue(activity, null, project, issue);
    }

    public static void navigateToAddIssue(Activity activity, View fab, Project project) {
        navigateToAddIssue(activity, fab, project, null);
    }

    private static void navigateToAddIssue(Activity activity, View fab, Project project, Issue issue) {
        Intent intent = NewIssuePopupDialog.newIntent(activity, project, issue);
        if (Build.VERSION.SDK_INT >= 21 && fab != null) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation
                    (activity, fab, activity.getString(R.string.transition_morph));
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
        }
    }
}
