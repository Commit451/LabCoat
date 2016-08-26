package com.commit451.gitlab;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.bluelinelabs.logansquare.LoganSquare;
import com.commit451.gitlab.api.GitLab;
import com.commit451.gitlab.model.api.FileUploadResponse;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.model.api.RepositoryTreeObject;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.util.FileUtil;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Response;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests account login and basic retrieval stuff
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, shadows = NetworkSecurityPolicyWorkaround.class)
public class ApiTests {

    private static final long FAKE_GROUP_PROJECT_ID = 376651;

    private static Project sFakeProject;
    private static GitLab gitLab;

    @BeforeClass
    public static void setUp() throws Exception {
        //for logging
        ShadowLog.stream = System.out;

        LoganSquare.registerTypeConverter(Uri.class, new NullTypeConverter());

        gitLab = TestUtil.login();

        Response<Project> projectResponse = gitLab
                .getProject(String.valueOf(FAKE_GROUP_PROJECT_ID))
                .execute();
        assertTrue(projectResponse.isSuccessful());
        assertNotNull(projectResponse.body());

        sFakeProject = projectResponse.body();
    }

    @Test
    public void getProjects() throws Exception {
        Response<List<Project>> projectsResponse = gitLab
                .getAllProjects()
                .execute();
        TestUtil.assertRetrofitResponseSuccess(projectsResponse);
        assertNotNull(projectsResponse.body());
    }

    @Test
    public void getGroups() throws Exception {
        Response<List<Group>> groupResponse = gitLab
                .getGroups()
                .execute();
        TestUtil.assertRetrofitResponseSuccess(groupResponse);
        assertNotNull(groupResponse.body());
    }

    @Test
    public void getIssues() throws Exception {
        String defaultState = RuntimeEnvironment.application.getResources().getString(R.string.issue_state_value_default);
        Response<List<Issue>> issuesResponse = gitLab
                .getIssues(sFakeProject.getId(), defaultState)
                .execute();
        TestUtil.assertRetrofitResponseSuccess(issuesResponse);
        assertNotNull(issuesResponse.body());
    }

    @Test
    public void getFiles() throws Exception {
        String defaultBranch = "master";
        String currentPath = "";
        Response<List<RepositoryTreeObject>> treeResponse = gitLab
                .getTree(sFakeProject.getId(), defaultBranch, currentPath)
                .execute();
        TestUtil.assertRetrofitResponseSuccess(treeResponse);
        assertNotNull(treeResponse.body());
    }

    @Test
    public void getCommits() throws Exception {
        String defaultBranch = "master";
        Response<List<RepositoryCommit>> commitsResponse = gitLab
                .getCommits(sFakeProject.getId(), defaultBranch, 0)
                .execute();
        TestUtil.assertRetrofitResponseSuccess(commitsResponse);
        assertNotNull(commitsResponse.body());
    }

    @Test
    public void getMergeRequests() throws Exception {
        String defaultState = RuntimeEnvironment.application.getResources().getString(R.string.merge_request_state_value_default);
        Response<List<MergeRequest>> mergeRequestResponse = gitLab
                .getMergeRequests(sFakeProject.getId(), defaultState)
                .execute();
        TestUtil.assertRetrofitResponseSuccess(mergeRequestResponse);
        assertNotNull(mergeRequestResponse.body());
    }

    @Test
    public void getCurrentUser() throws Exception {
        Response<UserFull> userFullResponse = gitLab
                .getThisUser()
                .execute();
        TestUtil.assertRetrofitResponseSuccess(userFullResponse);
        assertNotNull(userFullResponse.body());
    }

//    @Test
    public void uploadFile() throws Exception {
        Bitmap bitmap = BitmapFactory.decodeResource(RuntimeEnvironment.application.getResources(), R.drawable.ic_fork);
        MultipartBody.Part part = FileUtil.toPart(bitmap, "fork.png");

        Response<FileUploadResponse> uploadResponseResponse =
                gitLab.uploadFile(sFakeProject.getId(), part).execute();
        assertTrue(uploadResponseResponse.isSuccessful());
        assertNotNull(uploadResponseResponse.body());
    }

}