package com.commit451.gitlab.api

import android.content.Intent
import android.widget.Toast
import com.commit451.gitlab.App
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.LoginActivity
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.util.ThreadUtil
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import java.io.IOException

/**
 * If it detects a 401, redirect the user to the login screen, clearing activity the stack.
 * Kinda a weird global way of forcing the user to the login screen if their auth has expired
 */
class OpenSignInAuthenticator(private val account: Account) : Authenticator {

    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response): Request? {
        val url = response.request.url

        var cleanUrl = url.toString().toLowerCase()
        cleanUrl = cleanUrl.substring(cleanUrl.indexOf(':'))

        var cleanServerUrl = account.serverUrl.toString().toLowerCase()
        cleanServerUrl = cleanServerUrl.substring(cleanServerUrl.indexOf(':'))

        //Ensure that we only check urls of the gitlab instance
        if (cleanUrl.startsWith(cleanServerUrl)) {
            //Special case for if someone just put in their username or password wrong
            if ("session" != url.pathSegments[url.pathSegments.size - 1]) {
                //Off the background thread
                Timber.wtf(RuntimeException("Got a 401 and showing sign in for url: " + response.request.url))
                ThreadUtil.postOnMainThread(Runnable {
                    //Remove the account, so that the user can sign in again
                    Prefs.removeAccount(account)
                    Toast.makeText(App.get(), R.string.error_401, Toast.LENGTH_LONG)
                            .show()
                    val intent = LoginActivity.newIntent(App.get())
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    App.get().startActivity(intent)
                })
            }
        }
        return null
    }
}
