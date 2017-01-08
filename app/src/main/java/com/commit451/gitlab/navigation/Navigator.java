package com.commit451.gitlab.navigation;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.ImageView;

import com.commit451.easel.Easel;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.AboutActivity;
import com.commit451.gitlab.activity.ActivityActivity;
import com.commit451.gitlab.activity.AddIssueActivity;
import com.commit451.gitlab.activity.AddLabelActivity;
import com.commit451.gitlab.activity.AddMilestoneActivity;
import com.commit451.gitlab.activity.AddNewLabelActivity;
import com.commit451.gitlab.activity.AddUserActivity;
import com.commit451.gitlab.activity.AttachActivity;
import com.commit451.gitlab.activity.BuildActivity;
import com.commit451.gitlab.activity.DiffActivity;
import com.commit451.gitlab.activity.FileActivity;
import com.commit451.gitlab.activity.GroupActivity;
import com.commit451.gitlab.activity.GroupsActivity;
import com.commit451.gitlab.activity.IssueActivity;
import com.commit451.gitlab.activity.LoginActivity;
import com.commit451.gitlab.activity.MergeRequestActivity;
import com.commit451.gitlab.activity.MilestoneActivity;
import com.commit451.gitlab.activity.PickBranchOrTagActivity;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.activity.ProjectsActivity;
import com.commit451.gitlab.activity.SearchActivity;
import com.commit451.gitlab.activity.SettingsActivity;
import com.commit451.gitlab.activity.TodosActivity;
import com.commit451.gitlab.activity.UserActivity;
import com.commit451.gitlab.activity.WebLoginActivity;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.Ref;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Milestone;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.util.IntentUtil;
import com.commit451.morphtransitions.FabTransform;

import timber.log.Timber;

/**
 * Manages navigation so that we can override things as needed
 */
public class Navigator {

    public static void navigateToAbout(Activity activity) {
        activity.startActivity(AboutActivity.Companion.newIntent(activity));
    }

    public static void navigateToSettings(Activity activity) {
        activity.startActivity(SettingsActivity.newIntent(activity));
    }

    public static void navigateToProject(Activity activity, Project project) {
        activity.startActivity(ProjectActivity.newIntent(activity, project));
    }

    public static void navigateToProject(Activity activity, String projectId) {
        activity.startActivity(ProjectActivity.newIntent(activity, projectId));
    }

    public static void navigateToProject(Activity activity, String projectNamespace, String projectName) {
        activity.startActivity(ProjectActivity.newIntent(activity, projectNamespace, projectName));
    }

    public static void navigateToPickBranchOrTag(Activity activity, long projectId, @Nullable Ref currentRef, int requestCode) {
        activity.startActivityForResult(PickBranchOrTagActivity.newIntent(activity, projectId, currentRef), requestCode);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
    }

    public static void navigateToStartingActivity(Activity activity) {
        int startingActivity = App.get().getPrefs().getStartingView();
        switch (startingActivity) {
            case Prefs.STARTING_VIEW_PROJECTS:
                navigateToProjects(activity);
                break;
            case Prefs.STARTING_VIEW_GROUPS:
                navigateToGroups(activity);
                break;
            case Prefs.STARTING_VIEW_ACTIVITY:
                navigateToActivity(activity);
                break;
            case Prefs.STARTING_VIEW_TODOS:
                navigateToTodos(activity);
                break;
            default:
                throw new IllegalArgumentException("You need to define start activity " + startingActivity);
        }
    }

    public static void navigateToProjects(Activity activity) {
        activity.startActivity(ProjectsActivity.newIntent(activity));
    }

    public static void navigateToGroups(Activity activity) {
        activity.startActivity(GroupsActivity.newIntent(activity));
    }

    public static void navigateToActivity(Activity activity) {
        activity.startActivity(ActivityActivity.Companion.newIntent(activity));
    }

    public static void navigateToTodos(Activity activity) {
        activity.startActivity(TodosActivity.newIntent(activity));
    }

    public static void navigateToLogin(Activity activity) {
        activity.startActivity(LoginActivity.newIntent(activity));
    }

    public static void navigateToLogin(Activity activity, boolean showClose) {
        activity.startActivity(LoginActivity.newIntent(activity, showClose));
    }

    public static void navigateToWebSignin(Activity activity, String url, boolean extractingPrivateToken, int requestCode) {
        Intent intent = WebLoginActivity.newIntent(activity, url, extractingPrivateToken);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void navigateToSearch(Activity activity) {
        activity.startActivity(SearchActivity.newIntent(activity));
    }

    public static void navigateToUser(Activity activity, UserBasic user) {
        navigateToUser(activity, null, user);
    }

    public static void navigateToUser(Activity activity, ImageView profileImage, UserBasic user) {
        Intent intent = UserActivity.newIntent(activity, user);
        if (Build.VERSION.SDK_INT >= 21 && profileImage != null) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(activity, profileImage, activity.getString(R.string.transition_user));
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    public static void navigateToGroup(Activity activity, ImageView profileImage, Group group) {
        Intent intent = GroupActivity.newIntent(activity, group);
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(activity, profileImage, activity.getString(R.string.transition_user));
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    public static void navigateToGroup(Activity activity, long groupId) {
        activity.startActivity(GroupActivity.newIntent(activity, groupId));
    }

    public static void navigateToMilestone(Activity activity, Project project, Milestone milestone) {
        activity.startActivity(MilestoneActivity.newIntent(activity, project, milestone));
    }

    public static void navigateToIssue(Activity activity, Project project, Issue issue) {
        activity.startActivity(IssueActivity.newIntent(activity, project, issue));
    }

    public static void navigateToIssue(Activity activity, String namespace, String projectName, String issueIid) {
        activity.startActivity(IssueActivity.newIntent(activity, namespace, projectName, issueIid));
    }

    public static void navigateToMergeRequest(Activity activity, Project project, MergeRequest mergeRequest) {
        Intent intent = MergeRequestActivity.newIntent(activity, project, mergeRequest);
        activity.startActivity(intent);
    }

    public static void navigateToFile(Activity activity, long projectId, String path, String branchName) {
        activity.startActivity(FileActivity.newIntent(activity, projectId, path, branchName));
    }

    public static void navigateToDiffActivity(Activity activity, Project project, RepositoryCommit commit) {
        activity.startActivity(DiffActivity.newIntent(activity, project, commit));
    }

    public static void navigateToAddProjectMember(Activity activity, View fab, long projectId) {
        Intent intent = AddUserActivity.newIntent(activity, projectId);
        startMorphActivity(activity, fab, R.drawable.ic_add_24dp, intent);
    }

    public static void navigateToAddGroupMember(Activity activity, View fab, Group group) {
        Intent intent = AddUserActivity.newIntent(activity, group);
        startMorphActivity(activity, fab, R.drawable.ic_add_24dp, intent);
    }

    public static void navigateToEditIssue(Activity activity, View fab, Project project, Issue issue) {
        Intent intent = AddIssueActivity.Companion.newIntent(activity, project, issue);
        activity.startActivity(intent);
    }

    public static void navigateToAddIssue(Activity activity, View fab, Project project) {
        navigateToAddIssue(activity, fab, R.drawable.ic_add_24dp, project, null);
    }

    private static void navigateToAddIssue(Activity activity, View fab, @DrawableRes int drawable, Project project, Issue issue) {
        Intent intent = AddIssueActivity.Companion.newIntent(activity, project, issue);
        startMorphActivity(activity, fab, drawable, intent);
    }

    public static void navigateToAddLabels(Activity activity, Project project, int requestCode) {
        Intent intent = AddLabelActivity.newIntent(activity, project.getId());
        activity.startActivityForResult(intent, requestCode);
    }

    public static void navigateToAddNewLabel(Activity activity, long projectId, int requestCode) {
        Intent intent = AddNewLabelActivity.newIntent(activity, projectId);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void navigateToAddMilestone(Activity activity, View fab, Project project) {
        Intent intent = AddMilestoneActivity.newIntent(activity, project.getId());
        startMorphActivity(activity, fab, R.drawable.ic_add_24dp, intent);
    }

    public static void navigateToEditMilestone(Activity activity, View fab, Project project, Milestone milestone) {
        Intent intent = AddMilestoneActivity.newIntent(activity, project.getId(), milestone);
        activity.startActivity(intent);
    }

    public static void navigateToBuild(Activity activity, Project project, com.commit451.gitlab.model.api.Build build) {
        Intent intent = BuildActivity.newIntent(activity, project, build);
        activity.startActivity(intent);
    }

    public static void navigateToAttach(Activity activity, Project project, int requestCode) {
        Intent intent = AttachActivity.newIntent(activity, project);
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
    }

    private static void startMorphActivity(Activity activity, View fab, @DrawableRes int drawableRes, Intent intent) {
        if (Build.VERSION.SDK_INT >= 21 && fab != null) {
            FabTransform.addExtras(intent, Easel.getThemeAttrColor(activity, R.attr.colorAccent),
                    drawableRes);
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
            activity.startActivity(DeepLinker.generateDeeplinkIntentFromUri(activity, uri));
        } else {
            IntentUtil.openPage(activity, uri.toString());
        }
    }

    /**
     * Like {@link #navigateToUrl(Activity, Uri, Account)} but we already know it is the same server
     *
     * @param context context
     * @param uri     uri
     */
    public static void navigateToUrl(Context context, Uri uri) {
        Timber.d("navigateToUrl: %s", uri);
        context.startActivity(DeepLinker.generateDeeplinkIntentFromUri(context, uri));
    }
}
