package com.commit451.gitlab

import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.api.GitLabFactory
import com.commit451.gitlab.api.GitLabService
import com.commit451.gitlab.api.OkHttpClientFactory
import com.commit451.gitlab.model.Account
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert
import retrofit2.Response

/**
 * Util for testing
 */
object TestUtil {

    @Throws(Exception::class)
    fun login(): GitLab {
        // log in
        val account = Account()
        account.serverUrl = "https://gitlab.com/"

        val gitlabClientBuilder = OkHttpClientFactory.create(account)
        gitlabClientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        val gitLab = GitLabFactory.createGitLab(account, gitlabClientBuilder)
        // plz leave this account alone :/
        account.privateToken = "UjssYTJLWy7CFs7G8sS_"
        return gitLab
    }

    @Throws(Exception::class)
    fun assertRetrofitResponseSuccess(response: Response<*>) {
        if (!response.isSuccessful) {
            Assert.assertTrue(response.errorBody()!!.string(), response.isSuccessful)
        }
    }
}
