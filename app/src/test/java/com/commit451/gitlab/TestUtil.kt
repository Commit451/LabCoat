package com.commit451.gitlab

import com.commit451.gitlab.api.GitLabFactory
import com.commit451.gitlab.api.GitLabService
import com.commit451.gitlab.api.OkHttpClientFactory
import com.commit451.gitlab.api.request.SessionRequest
import com.commit451.gitlab.model.Account
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import retrofit2.Response

/**
 * Util for testing
 */
object TestUtil {

    @Throws(Exception::class)
    fun login(): GitLabService {
        //log in
        val account = Account()
        account.serverUrl = "https://gitlab.com/"

        val gitlabClientBuilder = OkHttpClientFactory.create(account)
        if (BuildConfig.DEBUG) {
            gitlabClientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        val gitLab = GitLabFactory.create(account, gitlabClientBuilder.build())
        val request = SessionRequest()
        request.login = "TestAllTheThings"
        request.password = "testing123"
        val loginResponse = gitLab
                .login(request)
                .blockingGet()
        assertTrue(loginResponse.isSuccessful)
        assertNotNull(loginResponse.body()!!.privateToken)
        //attach the newly retrieved private token
        account.privateToken = loginResponse.body()!!.privateToken
        return gitLab
    }

    @Throws(Exception::class)
    fun assertRetrofitResponseSuccess(response: Response<*>) {
        if (!response.isSuccessful) {
            Assert.assertTrue(response.errorBody()!!.string(), response.isSuccessful)
        }
    }
}
