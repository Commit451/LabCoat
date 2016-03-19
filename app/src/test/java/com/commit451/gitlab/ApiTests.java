package com.commit451.gitlab;

import android.net.Uri;

import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.model.api.RepositoryTreeObject;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.model.api.UserLogin;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import retrofit2.Response;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests account login and basic retrieval stuff
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ApiTests {

    private static final long FAKE_GROUP_PROJECT_ID = 376651;

    private static Project sFakeProject;

    @BeforeClass
    public static void setUp() throws Exception {
        //for logging
        ShadowLog.stream = System.out;
        //log in
        Account testAccount = getTestAccount();

        Response<UserLogin> loginResponse = GitLabClient.instance(testAccount)
                .loginWithUsername("TestAllTheThings", "testing123")
                .execute();
        assertTrue(loginResponse.isSuccessful());
        assertNotNull(loginResponse.body().getPrivateToken());
        //attach the newly retrieved private token
        testAccount.setPrivateToken(loginResponse.body().getPrivateToken());
        GitLabClient.setAccount(testAccount);

        Response<Project> projectResponse = GitLabClient.instance()
                .getProject(FAKE_GROUP_PROJECT_ID)
                .execute();
        assertTrue(projectResponse.isSuccessful());
        assertNotNull(projectResponse.body());

        sFakeProject = projectResponse.body();
    }

    private static Account getTestAccount() {
        Account account = new Account();
        account.setServerUrl(Uri.parse("https://gitlab.com"));
        return account;
    }

    @Test
    public void getProjects() throws Exception {
        Response<List<Project>> projectsResponse = GitLabClient.instance()
                .getAllProjects()
                .execute();
        assertTrue(projectsResponse.isSuccessful());
        assertNotNull(projectsResponse.body());
    }

    @Test
    public void getGroups() throws Exception {
        Response<List<Group>> groupResponse = GitLabClient.instance()
                .getGroups()
                .execute();
        assertTrue(groupResponse.isSuccessful());
        assertNotNull(groupResponse.body());
    }

    @Test
    public void getIssues() throws Exception {
        String defaultState = RuntimeEnvironment.application.getResources().getString(R.string.issue_state_value_default);
        Response<List<Issue>> issuesResponse = GitLabClient.instance()
                .getIssues(sFakeProject.getId(), defaultState)
                .execute();
        assertTrue(issuesResponse.isSuccessful());
        assertNotNull(issuesResponse.body());
    }

    @Test
    public void getFiles() throws Exception {
        String defaultBranch = "master";
        String currentPath = "";
        Response<List<RepositoryTreeObject>> treeResponse = GitLabClient.instance()
                .getTree(sFakeProject.getId(), defaultBranch, currentPath)
                .execute();
        assertTrue(treeResponse.isSuccessful());
        assertNotNull(treeResponse.body());
    }

    @Test
    public void getCommits() throws Exception {
        String defaultBranch = "master";
        Response<List<RepositoryCommit>> commitsResponse = GitLabClient.instance()
                .getCommits(sFakeProject.getId(), defaultBranch, 0)
                .execute();
        assertTrue(commitsResponse.isSuccessful());
        assertNotNull(commitsResponse.body());
    }

    @Test
    public void getMergeRequests() throws Exception {
        String defaultState = RuntimeEnvironment.application.getResources().getString(R.string.merge_request_state_value_default);
        Response<List<MergeRequest>> mergeRequestResponse = GitLabClient.instance()
                .getMergeRequests(sFakeProject.getId(), defaultState)
                .execute();
        assertTrue(mergeRequestResponse.isSuccessful());
        assertNotNull(mergeRequestResponse.body());
    }

    @Test
    public void getCurrentUser() throws Exception {
        Response<UserFull> userFullResponse = GitLabClient.instance()
                .getThisUser()
                .execute();
        assertTrue(userFullResponse.isSuccessful());
        assertNotNull(userFullResponse.body());
    }

}