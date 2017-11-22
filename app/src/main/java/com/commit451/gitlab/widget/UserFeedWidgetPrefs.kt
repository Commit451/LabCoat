package com.commit451.gitlab.widget

import android.content.Context
import android.content.SharedPreferences
import com.commit451.gitlab.api.MoshiProvider
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
                val adapter = MoshiProvider.moshi.adapter<Account>(Account::class.java)
                return adapter.fromJson(accountsJson)
            } catch (e: Exception) {
                //why would this ever happen?!?!?1
                getSharedPrefs(context).edit().remove(widgetId.toString() + KEY_ACCOUNT).commit()
            }

        }
        return null
    }

    fun setAccount(context: Context, widgetId: Int, account: Account) {
        try {
            val adapter = MoshiProvider.moshi.adapter<Account>(Account::class.java)
            val json = adapter.toJson(account)
            getSharedPrefs(context)
                    .edit()
                    .putString(widgetId.toString() + KEY_ACCOUNT, json)
                    .commit()
        } catch (e: IOException) {
            //this wont happen! Right?!?!?!
        }

    }
}
