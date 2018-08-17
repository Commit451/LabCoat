package com.commit451.gitlab.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.navigation.DeepLinker
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.util.IntentUtil
import timber.log.Timber


/**
 * The easy way to do deep links. Just route everything here, and it does all the work.
 */
class RoutingActivity : BaseActivity() {

    var originalUri: Uri? = null

    val navigator = object : DeepLinker.Callbacks {
        override fun onRouteToIssue(projectNamespace: String, projectName: String, issueIid: String) {
            Timber.d("Routing to issue")
            startActivity(LoadSomeInfoActivity.newIssueIntent(this@RoutingActivity, projectNamespace, projectName, issueIid))
            overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
        }

        override fun onRouteToCommit(projectNamespace: String, projectName: String, commitSha: String) {
            Timber.d("Routing to commit")
            startActivity(LoadSomeInfoActivity.newIntent(this@RoutingActivity, projectNamespace, projectName, commitSha))
            overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
        }

        override fun onRouteToMergeRequest(projectNamespace: String, projectName: String, mergeRequestIid: String) {
            Timber.d("Routing to merge request")
            startActivity(LoadSomeInfoActivity.newMergeRequestIntent(this@RoutingActivity, projectNamespace, projectName, mergeRequestIid))
            overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
        }

        override fun onRouteToProject(projectNamespace: String, projectName: String, selection: DeepLinker.ProjectSelection) {
            Timber.d("Routing to project")
            Navigator.navigateToProject(this@RoutingActivity, projectNamespace, projectName, selection)
        }

        override fun onRouteToBuild(projectNamespace: String, projectName: String, buildNumber: String) {
            Timber.d("Routing to build")
            startActivity(LoadSomeInfoActivity.newBuildIntent(this@RoutingActivity, projectNamespace, projectName, java.lang.Long.valueOf(buildNumber)!!))
            overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
        }

        override fun onRouteToMilestone(projectNamespace: String, projectName: String, milestoneNumber: String) {
            Timber.d("Routing to milestone")
            startActivity(LoadSomeInfoActivity.newMilestoneIntent(this@RoutingActivity, projectNamespace, projectName, milestoneNumber))
            overridePendingTransition(R.anim.fade_in, R.anim.do_nothing)
        }

        override fun onRouteUnknown(url: String?) {
            Timber.d("Route unknown. Opening original Uri if it exists")
            if (url != null) {
                IntentUtil.openPage(this@RoutingActivity, url.toString(), App.get().currentAccount)
            } else {
                Toast.makeText(this@RoutingActivity, R.string.deeplink_navigate_error, Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun hasBrowsableLinks(): Boolean {
        return true
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null || intent.data == null) {
            Timber.e("No url was passed. How did that happen?")
            finish()
            return
        }
        //If it has an original uri, this means that it is an internal deep link and we
        //can still fall back to what the original uri was and just show it
        originalUri = intent.getParcelableExtra(DeepLinker.KEY_ORIGINAL_URL)
        val link = intent.data.toString()
        Timber.d("Received deep link %s", link)
        Timber.d("Original link was %s", originalUri)

        //okay so last thing, if the user has followed a link, but the user
        //is not actually signed in, we want to direct them to signin
        if (Prefs.getAccounts().isEmpty()) {
            Navigator.navigateToLogin(this)
            finish()
            return
        }
        DeepLinker.route(link, navigator)
        finish()
    }
}

