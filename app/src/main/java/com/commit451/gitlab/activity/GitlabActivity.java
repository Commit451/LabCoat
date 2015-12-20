package com.commit451.gitlab.activity;

import android.app.Activity;
import android.os.Bundle;

import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.util.NavigationManager;

import java.util.List;

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

        List<Account> accounts = Prefs.getAccounts(this);
        if(accounts.isEmpty()) {
            NavigationManager.navigateToLogin(this);
        } else {
            GitLabClient.setAccount(accounts.get(0));
            NavigationManager.navigateToProjects(this);
        }

        // Always finish this activity
        finish();
    }
}
