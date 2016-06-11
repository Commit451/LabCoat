package com.commit451.gitlab.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.navigation.DeepLinker;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.navigation.RoutingNavigator;
import com.commit451.gitlab.navigation.RoutingRouter;
import com.commit451.gitlab.util.IntentUtil;
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs;

import timber.log.Timber;

/**
 * The easy way to do deep links. Just route everything here, and it does all the work.
 */
public class RoutingActivity extends Activity {

    RoutingRouter mRouter;

    private final RoutingNavigator mNavigator = new RoutingNavigator() {
        @Override
        public void onRouteToIssue(String projectNamespace, String projectName, String issueIid) {
            Navigator.navigateToIssue(RoutingActivity.this, projectNamespace, projectName, issueIid);
        }

        @Override
        public void onRouteToCommit(String projectNamespace, String projectName, String commitSha) {
            startActivity(LoadSomeInfoActivity.newIntent(RoutingActivity.this, projectNamespace, projectName, commitSha));
            overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
        }

        @Override
        public void onRouteToMergeRequest(String projectNamespace, String projectName, String mergeRequestId) {
            startActivity(LoadSomeInfoActivity.newMergeRequestIntent(RoutingActivity.this, projectNamespace, projectName, mergeRequestId));
            overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
        }

        @Override
        public void onRouteToProject(String namespace, String projectId) {
            Navigator.navigateToProject(RoutingActivity.this, projectId);
        }

        @Override
        public void onRouteToBuild(String projectNamespace, String projectName, String buildNumber) {
            startActivity(LoadSomeInfoActivity.newBuildIntent(RoutingActivity.this, projectNamespace, projectName, Long.valueOf(buildNumber)));
            overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
        }

        @Override
        public void onRouteToMilestone(String projectNamespace, String projectName, String milestoneNumber) {
            startActivity(LoadSomeInfoActivity.newMilestoneIntent(RoutingActivity.this, projectNamespace, projectName, milestoneNumber));
            overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
        }

        @Override
        public void onRouteUnknown(Uri uri) {
            if (mOriginalUri != null) {
                IntentUtil.openPage(RoutingActivity.this, uri.toString());
            } else {
                Toast.makeText(RoutingActivity.this, R.string.deeplink_navigate_error, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    };

    Uri mOriginalUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SimpleChromeCustomTabs.getInstance().connectTo(this);
    }

    @Override
    protected void onPause() {
        if (SimpleChromeCustomTabs.getInstance().isConnected()) {
            SimpleChromeCustomTabs.getInstance().disconnectFrom(this);
        }
        super.onPause();
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getData() == null) {
            Timber.e("No url was passed. How did that happen?");
            finish();
            return;
        }
        //If it has an original uri, this means that it is an internal deep link and we
        //can still fall back to what the original uri was and just show it
        mOriginalUri = intent.getParcelableExtra(DeepLinker.EXTRA_ORIGINAL_URI);
        Uri link = intent.getData();
        Timber.d("Received deep link %s", link);
        Timber.d("Original link was %s", mOriginalUri);

        //okay so last thing, if the user has followed a link, but the user
        //is not actually signed in, we want to direct them to signin
        if (GitLabClient.getAccount() == null && Account.getAccounts(this).isEmpty()) {
            Navigator.navigateToLogin(this);
            finish();
            return;
        }
        mRouter = new RoutingRouter(mNavigator);
        mRouter.route(link);
        finish();
    }
}

