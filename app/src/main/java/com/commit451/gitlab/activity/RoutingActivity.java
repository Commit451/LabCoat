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
import com.commit451.gitlab.navigation.NavigationManager;
import com.commit451.gitlab.util.IntentUtil;

import timber.log.Timber;

/**
 * The easy way to do deep links. Just route everything here, and it does all the work.
 */
public class RoutingActivity extends Activity {

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

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getData() == null) {
            Timber.e("No url was passed. How did that happen?");
            finish();
            return;
        }
        //If it has an original uri, this means that it is an internal deep link and we
        //can still fall back to what the original uri was and just show it
        Uri originalUri = intent.getParcelableExtra(DeepLinker.EXTRA_ORIGINAL_URI);
        Uri link = intent.getData();
        Timber.d("Received deep link %s", link);
        Timber.d("Original link was %s", originalUri);

        //okay so last thing, if the user has followed a link, but the user
        //is not actually signed in, we want to direct them to signin
        if (GitLabClient.getAccount() == null && Account.getAccounts(this) == null) {
            NavigationManager.navigateToLogin(this);
            finish();
            return;
        }
        boolean handled = false;
        if (link.getPath().contains("issues")) {
            Timber.d("Parsing as issue uri");
            if (link.getLastPathSegment().equals("issues")) {
                //this means it was just a link to something like
                //gitlab.com/Commit451/LabCoat/issues
                launchProject(link);
                handled = true;
            } else {
                int indexOfIssuesPathSegment = -1;
                for (int i=0; i<link.getPathSegments().size(); i++) {
                    if (link.getPathSegments().get(i).equals("issues")) {
                        indexOfIssuesPathSegment = i;
                        break;
                    }
                }
                if (indexOfIssuesPathSegment != -1) {
                    //this is good, it means it is a link to an actual issue
                    String projectNamespace = link.getPathSegments().get(indexOfIssuesPathSegment - 2);
                    String projectName = link.getPathSegments().get(indexOfIssuesPathSegment - 1);
                    String lastSegment = link.getPathSegments().get(indexOfIssuesPathSegment + 1);
                    //We have to do this cause there can be args on the url, such as
                    //https://gitlab.com/Commit451/LabCoat/issues/158#note_4560580
                    String[] stuff = lastSegment.split("#");
                    String issueIid = stuff[0];
                    Timber.d("Navigating to project %s with issue number %s", projectName, issueIid);
                    NavigationManager.navigateToIssue(this, projectNamespace, projectName, issueIid);
                    handled = true;
                }
            }
        } else if (link.getPath().contains("commit")) {
            int indexOfCommitPathSegment = -1;
            for (int i=0; i<link.getPathSegments().size(); i++) {
                if (link.getPathSegments().get(i).equals("commit")) {
                    indexOfCommitPathSegment = i;
                    break;
                }
            }
            if (indexOfCommitPathSegment != -1 && link.getPathSegments().size() > indexOfCommitPathSegment) {
                String projectNamespace = link.getPathSegments().get(indexOfCommitPathSegment-2);
                String projectName = link.getPathSegments().get(indexOfCommitPathSegment-1);
                String commitSha = link.getPathSegments().get(indexOfCommitPathSegment+1);
                startActivity(LoadSomeInfoActivity.newIntent(this, projectNamespace, projectName, commitSha));
                overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
                handled = true;
            }
        } else if (link.getPath().contains("commits")) {
            launchProject(link);
            handled = true;
        } else if (link.getPath().contains("compare")) {
            //comparing two commit shas
            String[] shas = link.getLastPathSegment().split("...");
            //TODO do the rest
        } else if (link.getPath().contains("merge_requests")) {
            for (int i=0; i<link.getPathSegments().size(); i++) {
                if (link.getPathSegments().get(i).equals("merge_requests")) {
                    if (i < link.getPathSegments().size() - 1) {
                        String projectNamespace = link.getPathSegments().get(i-2);
                        String projectName = link.getPathSegments().get(i-1);
                        String mergeRequestId = link.getPathSegments().get(i+1);
                        startActivity(LoadSomeInfoActivity.newMergeRequestIntent(this, projectNamespace, projectName, mergeRequestId));
                        overridePendingTransition(R.anim.fade_in, R.anim.do_nothing);
                        handled = true;
                        break;
                    } else {
                        launchProject(link);
                        handled = true;
                        break;
                    }
                }
            }
        }

        if (!handled) {
            if (originalUri != null) {
                launchOriginalUri(originalUri);
            } else {
                showError();
            }
        }
        finish();
    }

    private void launchProject(Uri uri) {
        String projectId = uri.getPathSegments().get(2);
        NavigationManager.navigateToProject(this, projectId);
    }

    private void launchOriginalUri(Uri uri) {
        IntentUtil.openPage(this, uri.toString());
    }

    private void showError() {
        Toast.makeText(RoutingActivity.this, R.string.deeplink_navigate_error, Toast.LENGTH_SHORT)
                .show();
    }
}

