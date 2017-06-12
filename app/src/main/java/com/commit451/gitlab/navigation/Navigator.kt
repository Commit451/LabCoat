package com.commit451.gitlab.navigation

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import android.widget.ImageView
import com.commit451.easel.Easel
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.*
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.model.Ref
import com.commit451.gitlab.model.api.*
import com.commit451.gitlab.util.IntentUtil
import com.commit451.morphtransitions.FabTransform
import timber.log.Timber

/**
 * Manages navigation so that we can override things as needed
 */
object Navigator {

    fun navigateToAbout(activity: Activity) {
        activity.startActivity(AboutActivity.newIntent(activity))
    }

    fun navigateToSettings(activity: Activity) {
        activity.startActivity(SettingsActivity.newIntent(activity))
    }

    fun navigateToProject(activity: Activity, project: Project) {
        activity.startActivity(ProjectActivity.newIntent(activity, project))
    }

    fun navigateToProject(activity: Activity, projectId: String) {
        activity.startActivity(ProjectActivity.newIntent(activity, projectId))
    }

    fun navigateToProject(activity: Activity, projectNamespace: String, projectName: String) {
        activity.startActivity(ProjectActivity.newIntent(activity, projectNamespace, projectName))
    }

    fun navigateToPickBranchOrTag(activity: Activity, projectId: Long, currentRef: Ref?, requestCode: Int) {
        activity.startActivityForResult(PickBranchOrTagActivity.newIntent(activity, projectId, currentRef), requestCode)
        activity.overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
    }

    fun navigateToStartingActivity(activity: Activity) {
        val startingActivity = Prefs.startingView
        when (startingActivity) {
            Prefs.STARTING_VIEW_PROJECTS -> navigateToProjects(activity)
            Prefs.STARTING_VIEW_GROUPS -> navigateToGroups(activity)
            Prefs.STARTING_VIEW_ACTIVITY -> navigateToActivity(activity)
            Prefs.STARTING_VIEW_TODOS -> navigateToTodos(activity)
            else -> throw IllegalArgumentException("You need to define start activity " + startingActivity)
        }
    }

    fun navigateToProjects(activity: Activity) {
        activity.startActivity(ProjectsActivity.newIntent(activity))
    }

    fun navigateToGroups(activity: Activity) {
        activity.startActivity(GroupsActivity.newIntent(activity))
    }

    fun navigateToActivity(activity: Activity) {
        activity.startActivity(ActivityActivity.newIntent(activity))
    }

    fun navigateToTodos(activity: Activity) {
        activity.startActivity(TodosActivity.newIntent(activity))
    }

    fun navigateToLogin(activity: Activity) {
        activity.startActivity(LoginActivity.newIntent(activity))
    }

    fun navigateToLogin(activity: Activity, showClose: Boolean) {
        activity.startActivity(LoginActivity.newIntent(activity, showClose))
    }

    fun navigateToWebSignin(activity: Activity, url: String, extractingPrivateToken: Boolean, requestCode: Int) {
        val intent = WebLoginActivity.newIntent(activity, url, extractingPrivateToken)
        activity.startActivityForResult(intent, requestCode)
    }

    fun navigateToSearch(activity: Activity) {
        activity.startActivity(SearchActivity.newIntent(activity))
    }

    fun navigateToUser(activity: Activity, user: UserBasic) {
        navigateToUser(activity, null, user)
    }

    fun navigateToUser(activity: Activity, profileImage: ImageView?, user: UserBasic) {
        val intent = UserActivity.newIntent(activity, user)
        if (Build.VERSION.SDK_INT >= 21 && profileImage != null) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, profileImage, activity.getString(R.string.transition_user))
            activity.startActivity(intent, options.toBundle())
        } else {
            activity.startActivity(intent)
        }
    }

    fun navigateToGroup(activity: Activity, profileImage: ImageView, group: Group) {
        val intent = GroupActivity.newIntent(activity, group)
        if (Build.VERSION.SDK_INT >= 21) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, profileImage, activity.getString(R.string.transition_user))
            activity.startActivity(intent, options.toBundle())
        } else {
            activity.startActivity(intent)
        }
    }

    fun navigateToGroup(activity: Activity, groupId: Long) {
        activity.startActivity(GroupActivity.newIntent(activity, groupId))
    }

    fun navigateToMilestone(activity: Activity, project: Project, milestone: Milestone) {
        activity.startActivity(MilestoneActivity.newIntent(activity, project, milestone))
    }

    fun navigateToIssue(activity: Activity, project: Project, issue: Issue) {
        activity.startActivity(IssueActivity.newIntent(activity, project, issue))
    }

    fun navigateToIssue(activity: Activity, namespace: String, projectName: String, issueIid: String) {
        activity.startActivity(IssueActivity.newIntent(activity, namespace, projectName, issueIid))
    }

    fun navigateToMergeRequest(activity: Activity, project: Project, mergeRequest: MergeRequest) {
        val intent = MergeRequestActivity.newIntent(activity, project, mergeRequest)
        activity.startActivity(intent)
    }

    fun navigateToFile(activity: Activity, projectId: Long, path: String, branchName: String) {
        activity.startActivity(FileActivity.newIntent(activity, projectId, path, branchName))
    }

    fun navigateToDiffActivity(activity: Activity, project: Project, commit: RepositoryCommit) {
        activity.startActivity(DiffActivity.newIntent(activity, project, commit))
    }

    fun navigateToAddProjectMember(activity: Activity, fab: View, projectId: Long) {
        val intent = AddUserActivity.newIntent(activity, projectId)
        startMorphActivity(activity, fab, R.drawable.ic_add_24dp, intent)
    }

    fun navigateToAddGroupMember(activity: Activity, fab: View, group: Group) {
        val intent = AddUserActivity.newIntent(activity, group)
        startMorphActivity(activity, fab, R.drawable.ic_add_24dp, intent)
    }

    fun navigateToEditIssue(activity: Activity, fab: View, project: Project, issue: Issue) {
        val intent = AddIssueActivity.newIntent(activity, project, issue)
        activity.startActivity(intent)
    }

    fun navigateToAddIssue(activity: Activity, fab: View, project: Project) {
        navigateToAddIssue(activity, fab, R.drawable.ic_add_24dp, project, null)
    }

    private fun navigateToAddIssue(activity: Activity, fab: View, @DrawableRes drawable: Int, project: Project, issue: Issue?) {
        val intent = AddIssueActivity.newIntent(activity, project, issue)
        startMorphActivity(activity, fab, drawable, intent)
    }

    fun navigateToAddLabels(activity: Activity, project: Project, requestCode: Int) {
        val intent = AddLabelActivity.newIntent(activity, project.id)
        activity.startActivityForResult(intent, requestCode)
    }

    fun navigateToAddNewLabel(activity: Activity, projectId: Long, requestCode: Int) {
        val intent = AddNewLabelActivity.newIntent(activity, projectId)
        activity.startActivityForResult(intent, requestCode)
    }

    fun navigateToAddMilestone(activity: Activity, fab: View, project: Project) {
        val intent = AddMilestoneActivity.newIntent(activity, project.id)
        startMorphActivity(activity, fab, R.drawable.ic_add_24dp, intent)
    }

    fun navigateToEditMilestone(activity: Activity, fab: View, project: Project, milestone: Milestone) {
        val intent = AddMilestoneActivity.newIntent(activity, project.id, milestone)
        activity.startActivity(intent)
    }

    fun navigateToBuild(activity: Activity, project: Project, build: com.commit451.gitlab.model.api.Build) {
        val intent = BuildActivity.newIntent(activity, project, build)
        activity.startActivity(intent)
    }
    fun navigateToPipeline(activity: Activity, project: Project, pipeline: com.commit451.gitlab.model.api.Pipeline) {
        val intent = PipelineActivity.newIntent(activity, project, pipeline)
        activity.startActivity(intent)
    }

    fun navigateToAttach(activity: Activity, project: Project, requestCode: Int) {
        val intent = AttachActivity.newIntent(activity, project)
        activity.startActivityForResult(intent, requestCode)
        activity.overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
    }

    private fun startMorphActivity(activity: Activity, fab: View?, @DrawableRes drawableRes: Int, intent: Intent) {
        if (Build.VERSION.SDK_INT >= 21 && fab != null) {
            FabTransform.addExtras(intent, Easel.getThemeAttrColor(activity, R.attr.colorAccent),
                    drawableRes)
            val options = ActivityOptions.makeSceneTransitionAnimation(activity, fab, activity.getString(R.string.transition_morph))
            activity.startActivity(intent, options.toBundle())
        } else {
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
        }
    }

    fun navigateToUrl(activity: Activity, uri: Uri, account: Account) {
        Timber.d("navigateToUrl: %s", uri)
        val serverUri = Uri.parse(account.serverUrl)
        if (serverUri.host == uri.host) {
            activity.startActivity(DeepLinker.generateDeeplinkIntentFromUri(activity, uri))
        } else {
            IntentUtil.openPage(activity as BaseActivity, uri.toString())
        }
    }

    /**
     * Like [.navigateToUrl] but we already know it is the same server

     * @param context context
     * *
     * @param uri     uri
     */
    fun navigateToUrl(context: Context, uri: Uri) {
        Timber.d("navigateToUrl: %s", uri)
        context.startActivity(DeepLinker.generateDeeplinkIntentFromUri(context, uri))
    }
}
