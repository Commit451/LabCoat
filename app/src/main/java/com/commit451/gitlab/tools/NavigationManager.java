package com.commit451.gitlab.tools;

import android.app.Activity;

import com.commit451.gitlab.activities.AboutActivity;
import com.commit451.gitlab.activities.LoginActivity;
import com.commit451.gitlab.activities.ProjectActivity;
import com.commit451.gitlab.activities.ProjectsActivity;
import com.commit451.gitlab.activities.SearchActivity;
import com.commit451.gitlab.activities.UserActivity;
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

    public static void navigateToLogin(Activity activity) {
        activity.startActivity(LoginActivity.newInstance(activity));
    }

    public static void navigateToSearch(Activity activity) {
        activity.startActivity(SearchActivity.newInstance(activity));
    }

    public static void navigateToUser(Activity activity, User user) {
        activity.startActivity(UserActivity.newInstance(activity, user));
    }
}
