package com.commit451.gitlab.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.ssl.CustomKeyManager;

import java.util.List;

/**
 * This activity acts as switching platform for the application directing the user to the appropriate
 * activity based on their logged in state
 */
public class LaunchActivity extends Activity {

    //Figure out how this works, then reenable
    private static final boolean PRIVATE_KEY_ENABLED = false;

    private static final int REQUEST_DEVICE_AUTH = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Account> accounts = Account.getAccounts();
        if(accounts.isEmpty()) {
            Navigator.navigateToLogin(this);
            finish();
        } else if (App.get().getPrefs().isRequireDeviceAuth()) {
            showKeyguard();
        } else {
            if (PRIVATE_KEY_ENABLED) {
                loadPrivateKey(accounts, 0);
            } else {
                moveAlong();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_DEVICE_AUTH:
                if (resultCode == RESULT_OK) {
                    moveAlong();
                } else {
                    finish();
                }
                break;
        }
    }

    @TargetApi(21)
    private void showKeyguard() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(getString(R.string.device_auth_title), getString(R.string.device_auth_message));
        if (intent == null) {
            moveAlong();
        } else {
            startActivityForResult(intent, REQUEST_DEVICE_AUTH);
        }
    }

    private void moveAlong() {
        Navigator.navigateToStartingActivity(this);
        finish();
    }

    private void loadPrivateKey(final List<Account> accounts, final int i) {
        if (i >= accounts.size()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

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
}
