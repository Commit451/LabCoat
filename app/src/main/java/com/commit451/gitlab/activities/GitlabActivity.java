package com.commit451.gitlab.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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
            Intent login = new Intent(this, LoginActivity.class);
            login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(login);
        }
        else {
            // Load MainActivity
            Intent main = new Intent(this, MainActivity.class);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(main);
        }

        // Always finish this activity
        finish();
    }
}
