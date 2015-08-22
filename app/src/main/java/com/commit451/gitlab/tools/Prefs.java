package com.commit451.gitlab.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Shared prefs things
 * Created by Jawn on 7/28/2015.
 */
public class Prefs {

    private static final String LOGGED_IN = "logged_in";
    private static final String SERVER_URL = "server_url";
    private static final String USER_ID = "user_id";
    private static final String PRIVATE_TOKEN = "private_token";
    private static final String LAST_PROJECT = "last_project";
    private static final String LAST_BRANCH = "last_branch";

    private static SharedPreferences getSharedPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean isLoggedIn(Context context) {
        return getSharedPrefs(context).getBoolean(LOGGED_IN, false);
    }

    public static void setLoggedIn(Context context, boolean loggedIn) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putBoolean(LOGGED_IN, loggedIn);
        editor.commit();
    }

    public static void setServerUrl(Context context, String serverUrl) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(SERVER_URL, serverUrl);
        editor.commit();
    }

    public static String getServerUrl(Context context) {
        return getSharedPrefs(context).getString(SERVER_URL, "");
    }

    public static void setPrivateToken(Context context, String privateToken) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(PRIVATE_TOKEN, privateToken);
        editor.commit();
    }

    public static String getPrivateToken(Context context) {
        return getSharedPrefs(context).getString(PRIVATE_TOKEN, "");
    }

    public static void setLastProject(Context context, String lastProject) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(LAST_PROJECT, lastProject);
        editor.commit();
    }

    public static String getLastProject(Context context) {
        return getSharedPrefs(context).getString(LAST_PROJECT, "");
    }

    public static void setLastBranch(Context context, String lastBranch) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putString(LAST_BRANCH, lastBranch);
        editor.commit();
    }

    public static String getLastBranch(Context context) {
        return getSharedPrefs(context).getString(LAST_BRANCH, "");
    }

    public static void setUserId(Context context, long userId) {
        SharedPreferences.Editor editor = getSharedPrefs(context).edit();
        editor.putLong(USER_ID, userId);
        editor.commit();
    }

    public static long getUserId(Context context) {
        return getSharedPrefs(context).getLong(USER_ID, -1);
    }
}
