package com.commit451.gitlab.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.provider.GsonProvider;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared prefs things
 * Created by Jawn on 7/28/2015.
 */
public class Prefs {

    private static final String ACCOUNTS = "accounts";

    private static SharedPreferences getSharedPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static List<Account> getAccounts(Context context) {
        String accountsJson = getSharedPrefs(context).getString(ACCOUNTS, null);
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

    private static void setAccounts(Context context, List<Account> accounts) {
        getSharedPrefs(context)
                .edit()
                .putString(ACCOUNTS, GsonProvider.getInstance().toJson(accounts))
                .commit();
    }
}
