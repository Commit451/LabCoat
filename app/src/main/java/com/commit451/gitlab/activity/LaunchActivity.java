package com.commit451.gitlab.activity;

import android.app.Activity;
import android.os.Bundle;

import com.commit451.gitlab.BuildConfig;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.ssl.CustomKeyManager;
import com.commit451.gitlab.navigation.Navigator;

import java.util.List;

import timber.log.Timber;

/**
 * This activity acts as switching platform for the application directing the user to the appropriate
 * activity based on their logged in state
 */
public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int savedVersion = Prefs.getSavedVersion(this);
        if (savedVersion != -1 && savedVersion < BuildConfig.VERSION_CODE) {
            Timber.d("Performing upgrade");
            performUpgrade(savedVersion, BuildConfig.VERSION_CODE);
            Prefs.setSavedVersion(this);
        }
        List<Account> accounts = Account.getAccounts(this);
        if(accounts.isEmpty()) {
            Navigator.navigateToLogin(this);
        } else {
            loadPrivateKey(accounts, 0);
            return;
        }

        // Always finish this activity
        finish();
    }

    private void loadPrivateKey(final List<Account> accounts, final int i) {
        if (i >= accounts.size()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Navigator.navigateToStartingActivity(LaunchActivity.this);
                    finish();
                }
            });
            return;
        }

        String alias = accounts.get(i).getPrivateKeyAlias();
        if (alias != null && !CustomKeyManager.isCached(alias)) {
            CustomKeyManager.cache(this, alias, new CustomKeyManager.KeyCallback() {
                @Override
                public void onSuccess(CustomKeyManager.KeyEntry entry) {
                    loadPrivateKey(accounts, i + 1);
                }

                @Override
                public void onError(Exception e) {
                    loadPrivateKey(accounts, i + 1);
                }
            });
        } else {
            loadPrivateKey(accounts, i + 1);
        }
    }

    /**
     * Perform an upgrade from one version to another. This should only be one time upgrade things
     */
    private void performUpgrade(int previousVersion, int currentVersion) {

    }
}
