package com.commit451.gitlab.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.commit451.gitlab.BuildConfig;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.provider.GsonProvider;
import com.google.gson.reflect.TypeToken;

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

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STARTING_VIEW_PROJECTS, STARTING_VIEW_GROUPS, STARTING_VIEW_ACTIVITY})
    public @interface StartingView {}

    private static SharedPreferences getSharedPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static List<Account> getAccounts(Context context) {
        String accountsJson = getSharedPrefs(context).getString(KEY_ACCOUNTS, null);
        if (!TextUtils.isEmpty(accountsJson)) {
            return GsonProvider.getInstance().fromJson(accountsJson, new TypeToken<List<Account>>(){}.getType());
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
        getSharedPrefs(context)
                .edit()
                .putString(KEY_ACCOUNTS, GsonProvider.getInstance().toJson(accounts))
                .commit();
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
