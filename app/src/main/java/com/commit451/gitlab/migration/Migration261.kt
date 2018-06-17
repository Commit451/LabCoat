package com.commit451.gitlab.migration

import com.commit451.gitlab.App
import com.commit451.gitlab.data.Prefs
import io.reactivex.Completable
import java.util.*

/**
 * We started saving username and email to the user account in a certain update
 * so that we could more easily fetch projects, but we did not account for the
 * fact that users that were already signed in would not have these values stored.
 * This makes sure that we add these values if they do not exist.
 */
object Migration261 {

    fun run(): Completable {
        return Completable.defer {
            val user = App.get().gitLab.getThisUser()
                    .blockingGet()
                    .body()!!
            val currentAccount = App.get().currentAccount
            currentAccount.lastUsed = Date()
            currentAccount.email = user.email
            currentAccount.username = user.username
            Prefs.updateAccount(currentAccount)
            Completable.complete()
        }
    }
}