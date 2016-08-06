package com.commit451.gitlab.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;
import com.commit451.gitlab.BuildConfig;
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
    private static final String KEY_VERSION = "current_version";
    private static final String KEY_STARTING_VIEW = "starting_view";

    public static final int STARTING_VIEW_PROJECTS = 0;
    public static final int STARTING_VIEW_GROUPS = 1;
    public static final int STARTING_VIEW_ACTIVITY = 2;
    public static final int STARTING_VIEW_TODOS = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STARTING_VIEW_PROJECTS, STARTING_VIEW_GROUPS, STARTING_VIEW_ACTIVITY, STARTING_VIEW_TODOS})
    public @interface StartingView {}

    private static SharedPreferences getSharedPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    public static List<Account> getAccounts(Context context) {
        String accountsJson = getSharedPrefs(context).getString(KEY_ACCOUNTS, null);
        if (!TextUtils.isEmpty(accountsJson)) {
            try {
                return LoganSquare.parseList(accountsJson, Account.class);
            } catch (IOException e) {
                //why would this ever happen?!?!?1
                getSharedPrefs(context).edit().remove(KEY_ACCOUNTS).commit();
            }
            return new ArrayList<>();
        } else {
            return new ArrayList<>();
        }
    }

    public static void addAccount(Context context, Account account) {
        List<Account> accounts = getAccounts(context);
        accounts.add(account);
        setAccounts(context, accounts);
    }

    public static void removeAccount(Context context, Account account) {
        List<Account> accounts = getAccounts(context);
        accounts.remove(account);
        setAccounts(context, accounts);
    }

    public static void updateAccount(Context context, Account account) {
        List<Account> accounts = getAccounts(context);
        accounts.remove(account);
        accounts.add(account);
        setAccounts(context, accounts);
    }

    private static void setAccounts(Context context, List<Account> accounts) {
        try {
            String json = LoganSquare.serialize(accounts);
            getSharedPrefs(context)
                    .edit()
                    .putString(KEY_ACCOUNTS, json)
                    .commit();
        } catch (IOException e) {
            //this wont happen! Right?!?!?!
        }
    }

    public static int getSavedVersion(Context context) {
        return getSharedPrefs(context).getInt(KEY_VERSION, -1);
    }

    public static void setSavedVersion(Context context) {
        getSharedPrefs(context)
                .edit()
                .putInt(KEY_VERSION, BuildConfig.VERSION_CODE)
                .commit();
    }

    public static int getStartingView(Context context) {
        return getSharedPrefs(context).getInt(KEY_STARTING_VIEW, STARTING_VIEW_PROJECTS);
    }

    public static void setStartingView(Context context, int startingView) {
        getSharedPrefs(context)
                .edit()
                .putInt(KEY_STARTING_VIEW, startingView)
                .commit();
    }
}
