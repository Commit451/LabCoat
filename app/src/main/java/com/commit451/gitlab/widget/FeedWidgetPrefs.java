package com.commit451.gitlab.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.LoganSquare;
import com.commit451.gitlab.model.Account;

import java.io.IOException;

/**
 * The prefs for the feed widget
 */
public class FeedWidgetPrefs {

    public static String FILE_NAME = "LabCoatWidgetPrefs";

    private static final String KEY_ACCOUNT = "_account";

    private static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    public static Account getAccount(Context context, int widgetId) {
        String accountsJson = getSharedPrefs(context).getString(widgetId + KEY_ACCOUNT, null);
        if (!TextUtils.isEmpty(accountsJson)) {
            try {
                return LoganSquare.parse(accountsJson, Account.class);
            } catch (IOException e) {
                //why would this ever happen?!?!?1
                getSharedPrefs(context).edit().remove(widgetId + KEY_ACCOUNT).commit();
            }
        }
        return null;
    }

    public static void setAccount(Context context, int widgetId, Account account) {
        try {
            String json = LoganSquare.serialize(account);
            getSharedPrefs(context)
                    .edit()
                    .putString(widgetId + KEY_ACCOUNT, json)
                    .commit();
        } catch (IOException e) {
            //this wont happen! Right?!?!?!
        }
    }

}
