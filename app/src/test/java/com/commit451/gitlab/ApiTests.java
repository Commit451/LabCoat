package com.commit451.gitlab;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.bluelinelabs.logansquare.LoganSquare;
import com.commit451.gitlab.api.GitLab;
import com.commit451.gitlab.model.api.FileUploadResponse;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.Issue;
import com.commit451.gitlab.model.api.Member;
import com.commit451.gitlab.model.api.MergeRequest;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.model.api.RepositoryTreeObject;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.util.FileUtil;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Response;

import static org.junit.Assert.assertNotNull;

/**
 * Tests account login and basic retrieval stuff
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
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

        Project projectResponse = gitLab
                .getProject(String.valueOf(FAKE_GROUP_PROJECT_ID))
                .toBlocking()
                .first();
        assertNotNull(projectResponse);

        sFakeProject = projectResponse;
    }

    @Test
    public void getProjects() throws Exception {
        Response<List<Project>> projectsResponse = gitLab
                .getAllProjects()
                .toBlocking()
                .first();
        TestUtil.assertRetrofitResponseSuccess(projectsResponse);
        assertNotNull(projectsResponse.body());
    }

    @Test
    public void getGroups() throws Exception {
        Response<List<Group>> groupResponse = gitLab
                .getGroups()
                .toBlocking()
                .first();
        TestUtil.assertRetrofitResponseSuccess(groupResponse);
        assertNotNull(groupResponse.body());
    }

    @Test
    public void getGroupMembers() throws Exception {
        //GitLab group id
        long gitLabGroupId = 9970;
        Response<List<Member>> groupResponse = gitLab
                .getGroupMembers(gitLabGroupId)
                .toBlocking()
                .first();
        TestUtil.assertRetrofitResponseSuccess(groupResponse);
        assertNotNull(groupResponse.body());
    }

    @Test
    public void getIssues() throws Exception {
        String defaultState = RuntimeEnvironment.application.getResources().getString(R.string.issue_state_value_default);
        Response<List<Issue>> issuesResponse = gitLab
                .getIssues(sFakeProject.getId(), defaultState)
                .toBlocking()
                .first();
        TestUtil.assertRetrofitResponseSuccess(issuesResponse);
        assertNotNull(issuesResponse.body());
    }

    @Test
    public void getFiles() throws Exception {
        String defaultBranch = "master";
        String currentPath = "";
        List<RepositoryTreeObject> treeResponse = gitLab
                .getTree(sFakeProject.getId(), defaultBranch, currentPath)
                .toBlocking()
                .first();
        assertNotNull(treeResponse);
    }

    @Test
    public void getCommits() throws Exception {
        String defaultBranch = "master";
        List<RepositoryCommit> commitsResponse = gitLab
                .getCommits(sFakeProject.getId(), defaultBranch, 0)
                .toBlocking()
                .first();
        assertNotNull(commitsResponse);
    }

    @Test
    public void getMergeRequests() throws Exception {
        String defaultState = RuntimeEnvironment.application.getResources().getString(R.string.merge_request_state_value_default);
        Response<List<MergeRequest>> mergeRequestResponse = gitLab
                .getMergeRequests(sFakeProject.getId(), defaultState)
                .toBlocking()
                .first();
        TestUtil.assertRetrofitResponseSuccess(mergeRequestResponse);
        assertNotNull(mergeRequestResponse.body());
    }

    @Test
    public void getCurrentUser() throws Exception {
        Response<UserFull> userFullResponse = gitLab
                .getThisUser()
                .toBlocking()
                .first();
        TestUtil.assertRetrofitResponseSuccess(userFullResponse);
        assertNotNull(userFullResponse.body());
    }

//    @Test
    public void uploadFile() throws Exception {
        Bitmap bitmap = BitmapFactory.decodeResource(RuntimeEnvironment.application.getResources(), R.drawable.ic_fork);
        MultipartBody.Part part = FileUtil.toPart(bitmap, "fork.png");

        FileUploadResponse uploadResponseResponse =
                gitLab.uploadFile(sFakeProject.getId(), part)
                        .toBlocking().first();
        assertNotNull(uploadResponseResponse);
    }

}