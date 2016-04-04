package com.commit451.gitlab.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.commit451.gitlab.R;
import com.commit451.gitlab.util.DeepLinker;
import com.commit451.gitlab.util.IntentUtil;
import com.commit451.gitlab.util.NavigationManager;

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
        boolean handled = false;
        if (link.getPath().contains("issues")) {
            Timber.d("Parsing as issue uri");
            if (link.getPathSegments().size() == 3) {
                //this means it was just a link to something like
                //gitlab.com/Commit451/LabCoat/issues
                launchProject(link);
                handled = true;
            } else if (link.getPathSegments().size() == 4) {
                //this is good, it means it is a link to an actual issue
                handled = true;
                String projectNamespace = link.getPathSegments().get(0);
                String projectName = link.getPathSegments().get(1);
                String lastSegment = link.getPathSegments().get(3);
                //We have to do this cause there can be args on the url, such as
                //https://gitlab.com/Commit451/LabCoat/issues/158#note_4560580
                String[] stuff = lastSegment.split("#");
                String issueIid = stuff[0];
                Timber.d("Navigating to project %s with issue number %s", projectName, issueIid);
                NavigationManager.navigateToIssue(this, projectNamespace, projectName, issueIid);
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

