package com.commit451.gitlab;

import android.net.Uri;

import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.UserLogin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import retrofit.Response;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests account login and basic retrieval stuff
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class LoginUnitTest {

    @Test
    public void loginTest() throws Exception {
        Account testAccount = getTestAccount();

        Response<UserLogin> loginResponse = GitLabClient.instance(testAccount)
                .loginWithUsername("TestAllTheThings", "testing123")
                .execute();
        assertTrue(loginResponse.isSuccess());
        assertNotNull(loginResponse.body().getPrivateToken());
        //attach the newly retrieved private token
        testAccount.setPrivateToken(loginResponse.body().getPrivateToken());
        GitLabClient.setAccount(testAccount);
        Response<List<Project>> projectsResponse = GitLabClient.instance()
                .getAllProjects()
                .execute();
        assertTrue(projectsResponse.isSuccess());
        assertNotNull(projectsResponse.body());
    }

    public static Account getTestAccount() {
        Account account = new Account();
        account.setServerUrl(Uri.parse("https://gitlab.com"));
        return account;
    }
}