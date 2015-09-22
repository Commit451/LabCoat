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
    private static final String PRIVATE_TOKEN = "private_token";

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
        return getSharedPrefs(context).getString(PRIVATE_TOKEN, null);
    }
}
