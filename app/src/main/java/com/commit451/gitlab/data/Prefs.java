package com.commit451.gitlab.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;
import com.commit451.gitlab.model.Account;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared prefs things
 */
public class Prefs {

    private static final String KEY_ACCOUNTS = "accounts";
    private static final String KEY_STARTING_VIEW = "starting_view";
    private static final String KEY_REQUIRE_DEVICE_AUTH = "require_device_auth";

    public static final int STARTING_VIEW_PROJECTS = 0;
    public static final int STARTING_VIEW_GROUPS = 1;
    public static final int STARTING_VIEW_ACTIVITY = 2;
    public static final int STARTING_VIEW_TODOS = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STARTING_VIEW_PROJECTS, STARTING_VIEW_GROUPS, STARTING_VIEW_ACTIVITY, STARTING_VIEW_TODOS})
    public @interface StartingView {
    }

    private SharedPreferences prefs;

    public Prefs(Context context) {
        if (!(context instanceof Application)) {
            throw new IllegalArgumentException("This should be the application context. Not the activity context");
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    public List<Account> getAccounts() {
        String accountsJson = prefs.getString(KEY_ACCOUNTS, null);
        if (!TextUtils.isEmpty(accountsJson)) {
            try {
                return LoganSquare.parseList(accountsJson, Account.class);
            } catch (IOException e) {
                //why would this ever happen?!?!?1
                prefs.edit().remove(KEY_ACCOUNTS).apply();
            }
            return new ArrayList<>();
        } else {
            return new ArrayList<>();
        }
    }

    public void addAccount(Account account) {
        List<Account> accounts = getAccounts();
        accounts.add(account);
        setAccounts(accounts);
    }

    public void removeAccount(Account account) {
        List<Account> accounts = getAccounts();
        accounts.remove(account);
        setAccounts(accounts);
    }

    public void updateAccount(Account account) {
        List<Account> accounts = getAccounts();
        accounts.remove(account);
        accounts.add(account);
        setAccounts(accounts);
    }

    private void setAccounts(List<Account> accounts) {
        try {
            String json = LoganSquare.serialize(accounts);
            prefs.edit()
                    .putString(KEY_ACCOUNTS, json)
                    .apply();
        } catch (IOException e) {
            //this wont happen! Right?!?!?!
        }
    }

    @StartingView
    public int getStartingView() {
        @StartingView
        int start = prefs.getInt(KEY_STARTING_VIEW, STARTING_VIEW_PROJECTS);
        return start;
    }

    public void setStartingView(@StartingView int startingView) {
        prefs.edit()
                .putInt(KEY_STARTING_VIEW, startingView)
                .apply();
    }

    public boolean isRequireDeviceAuth() {
        return prefs.getBoolean(KEY_REQUIRE_DEVICE_AUTH, false);
    }

    public void setRequireDeviceAuth(boolean require) {
        prefs.edit()
                .putBoolean(KEY_REQUIRE_DEVICE_AUTH, require)
                .apply();
    }
}
