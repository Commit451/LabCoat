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
public class ProjectFeedWidgetPrefs {

    public static String FILE_NAME = "LabCoatProjectWidgetPrefs";

    private static final String KEY_ACCOUNT = "_account";
    private static final String KEY_PROJECT_FEED_URL = "_feed_url";

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

    @Nullable
    public static String getFeedUrl(Context context, int widgetId) {
        return getSharedPrefs(context).getString(widgetId + KEY_PROJECT_FEED_URL, null);
    }

    public static void setFeedUrl(Context context, int widgetId, String url) {
        getSharedPrefs(context)
                .edit()
                .putString(widgetId + KEY_PROJECT_FEED_URL, url)
                .commit();
    }
}
