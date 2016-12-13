package com.commit451.gitlab;

import android.net.Uri;

import com.commit451.gitlab.api.GitLab;
import com.commit451.gitlab.api.GitLabFactory;
import com.commit451.gitlab.api.OkHttpClientFactory;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.UserLogin;

import org.junit.Assert;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Util for testing
 */
public class TestUtil {

    public static GitLab login() throws Exception {
        //log in
        Account account = new Account();
        account.setServerUrl(Uri.parse("https://gitlab.com/"));

        OkHttpClient.Builder gitlabClientBuilder = OkHttpClientFactory.create(account);
        if (BuildConfig.DEBUG) {
            gitlabClientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        GitLab gitLab = GitLabFactory.create(account, gitlabClientBuilder.build(), true);
        Response<UserLogin> loginResponse = gitLab
                .loginWithUsername("TestAllTheThings", "testing123")
                .blockingGet();
        assertTrue(loginResponse.isSuccessful());
        assertNotNull(loginResponse.body().getPrivateToken());
        //attach the newly retrieved private token
        account.setPrivateToken(loginResponse.body().getPrivateToken());
        return gitLab;
    }

    public static void assertRetrofitResponseSuccess(Response response) throws Exception {
        if (!response.isSuccessful()) {
            Assert.assertTrue(response.errorBody().string(), response.isSuccessful());
        }
    }
}
