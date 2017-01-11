package com.commit451.gitlab.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.annotation.IntDef
import com.bluelinelabs.logansquare.LoganSquare
import com.commit451.gitlab.model.Account
import java.io.IOException
import java.util.*

/**
 * Shared prefs things
 */
object Prefs {

    val KEY_ACCOUNTS = "accounts"
    val KEY_STARTING_VIEW = "starting_view"
    val KEY_REQUIRE_DEVICE_AUTH = "require_device_auth"

    const val STARTING_VIEW_PROJECTS = 0
    const val STARTING_VIEW_GROUPS = 1
    const val STARTING_VIEW_ACTIVITY = 2
    const val STARTING_VIEW_TODOS = 3

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(STARTING_VIEW_PROJECTS.toLong(), STARTING_VIEW_GROUPS.toLong(), STARTING_VIEW_ACTIVITY.toLong(), STARTING_VIEW_TODOS.toLong())
    annotation class StartingView

    lateinit private var prefs: SharedPreferences

    fun init(context : Context) {
        if (context !is Application) {
            throw IllegalArgumentException("This should be the application context. Not the activity context")
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getAccounts(): MutableList<Account> {
        val accountsJson = prefs.getString(KEY_ACCOUNTS, null)
        if (!accountsJson.isNullOrEmpty()) {
            try {
                return LoganSquare.parseList(accountsJson, Account::class.java)
            } catch (e: IOException) {
                prefs.edit().remove(KEY_ACCOUNTS).apply()
            }
            return ArrayList()
        } else {
            return ArrayList()
        }
    }

    fun setAccounts(accounts: List<Account>) {
        try {
            val json = LoganSquare.serialize<List<Account>>(accounts)
            prefs.edit()
                    .putString(KEY_ACCOUNTS, json)
                    .apply()
        } catch (e: IOException) {
            prefs.edit()
                    .remove(KEY_ACCOUNTS)
                    .apply()
        }
    }

    fun addAccount(account: Account) {
        val accounts = getAccounts()
        accounts.add(account)
        setAccounts(accounts)
    }

    fun removeAccount(account: Account) {
        val accounts = getAccounts()
        accounts.remove(account)
        setAccounts(accounts)
    }

    fun updateAccount(account: Account) {
        val accounts = getAccounts()
        accounts.remove(account)
        accounts.add(account)
        setAccounts(accounts)
    }

    fun getStartingView(): Int {
        @StartingView
        val start = prefs.getInt(KEY_STARTING_VIEW, STARTING_VIEW_PROJECTS)
        return start
    }

    fun setStartingView(@StartingView startingView: Int) {
        prefs.edit()
                .putInt(KEY_STARTING_VIEW, startingView)
                .apply()
    }

    fun isRequiredDeviceAuth(): Boolean {
        return prefs.getBoolean(KEY_REQUIRE_DEVICE_AUTH, false)
    }

    fun setRequiredDeviceAuth(require: Boolean) {
        prefs.edit()
                .putBoolean(KEY_REQUIRE_DEVICE_AUTH, require)
                .apply()
    }
}
