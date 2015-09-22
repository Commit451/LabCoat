package com.commit451.gitlab.activities;

import android.app.Activity;
import android.os.Bundle;

import com.commit451.gitlab.tools.NavigationManager;
import com.commit451.gitlab.tools.Prefs;

/**
 * This activity acts as switching platform for the application directing the user to the appropriate
 * activity based on their logged in state
 *
 * Created by r0adkll on 9/18/15.
 */
public class GitlabActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!Prefs.isLoggedIn(this)) {
            NavigationManager.navigateToLogin(this);
        } else {
            NavigationManager.navigateToProjects(this);
        }

        // Always finish this activity
        finish();
    }
}
