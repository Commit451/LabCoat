package com.commit451.gitlab.widget

import android.content.Context
import android.content.SharedPreferences
import com.bluelinelabs.logansquare.LoganSquare
import com.commit451.gitlab.model.Account
import java.io.IOException

/**
 * The prefs for the feed widget
 */
object UserFeedWidgetPrefs {

    var FILE_NAME = "LabCoatUsWidgetPrefs"

    private val KEY_ACCOUNT = "_account"

    private fun getSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    fun getAccount(context: Context, widgetId: Int): Account? {
        val accountsJson = getSharedPrefs(context).getString(widgetId.toString() + KEY_ACCOUNT, null)
        if (!accountsJson.isNullOrEmpty()) {
            try {
                return LoganSquare.parse(accountsJson, Account::class.java)
            } catch (e: IOException) {
                //why would this ever happen?!?!?1
                getSharedPrefs(context).edit().remove(widgetId.toString() + KEY_ACCOUNT).commit()
            }

        }
        return null
    }

    fun setAccount(context: Context, widgetId: Int, account: Account) {
        try {
            val json = LoganSquare.serialize(account)
            getSharedPrefs(context)
                    .edit()
                    .putString(widgetId.toString() + KEY_ACCOUNT, json)
                    .commit()
        } catch (e: IOException) {
            //this wont happen! Right?!?!?!
        }

    }

}
