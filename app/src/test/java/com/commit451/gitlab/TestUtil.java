package com.commit451.gitlab;

import android.net.Uri;

import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.UserLogin;

import retrofit2.Response;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Util for testing
 */
public class TestUtil {

    public static void login() throws Exception {
        //log in
        Account account = new Account();
        account.setServerUrl(Uri.parse("https://gitlab.com/"));

        Response<UserLogin> loginResponse = GitLabClient.create(account)
                .loginWithUsername("TestAllTheThings", "testing123")
                .execute();
        assertTrue(loginResponse.isSuccessful());
        assertNotNull(loginResponse.body().getPrivateToken());
        //attach the newly retrieved private token
        account.setPrivateToken(loginResponse.body().getPrivateToken());
        GitLabClient.setAccount(account);
    }
}
