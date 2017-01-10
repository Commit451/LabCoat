package com.commit451.gitlab.api;

import android.content.Intent;
import android.widget.Toast;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.LoginActivity;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.util.ThreadUtil;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import timber.log.Timber;

/**
 * If it detects a 401, redirect the user to the login screen, clearing activity the stack.
 * Kinda a weird global way of forcing the user to the login screen if their auth has expired
 */
public class OpenSignInAuthenticator implements Authenticator {

    private Account account;

    public OpenSignInAuthenticator(Account account) {
        this.account = account;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        //Special case for if someone just put in their username or password wrong
        if (!"session".equals(response.request().url().pathSegments().get(response.request().url().pathSegments().size()-1))) {
            //Off the background thread
            Timber.wtf(new RuntimeException("Got a 401 and showing sign in for url: " + response.request().url()));
            ThreadUtil.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    //Remove the account, so that the user can sign in again
                    App.Companion.get().getPrefs().removeAccount(account);
                    Toast.makeText(App.Companion.get(), R.string.error_401, Toast.LENGTH_LONG)
                            .show();
                    Intent intent = LoginActivity.Companion.newIntent(App.Companion.get());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    App.Companion.get().startActivity(intent);
                }
            });
        }
        return null;
    }
}
