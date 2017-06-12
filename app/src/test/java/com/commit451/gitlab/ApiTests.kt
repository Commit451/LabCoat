package com.commit451.gitlab

import android.graphics.BitmapFactory
import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.api.GitLabService
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.util.FileUtil
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

/**
 * Tests account login and basic retrieval stuff
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class ApiTests {

    companion object {

        private val FAKE_GROUP_PROJECT_ID: Long = 376651

        private var fakeProject: Project? = null
        private var gitLab: GitLabService? = null

        @JvmStatic
        @BeforeClass
        @Throws(Exception::class)
        fun setUp() {
            //for logging
            ShadowLog.stream = System.out

            GitLab.init()

            gitLab = TestUtil.login()

            val projectResponse = gitLab!!
                    .getProject(FAKE_GROUP_PROJECT_ID.toString())
                    .blockingGet()
            assertNotNull(projectResponse)

            fakeProject = projectResponse
        }
    }

    @Test
    @Throws(Exception::class)
    fun getProjects() {
        val projectsResponse = gitLab!!
                .getAllProjects()
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(projectsResponse)
        assertNotNull(projectsResponse.body())
    }

    @Test
    @Throws(Exception::class)
    fun getGroups() {
        val groupResponse = gitLab!!
                .getGroups()
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(groupResponse)
        assertNotNull(groupResponse.body())
    }

    @Test
    @Throws(Exception::class)
    fun getGroupMembers() {
        //GitLabService group id
        val gitLabGroupId: Long = 9970
        val groupResponse = gitLab!!
                .getGroupMembers(gitLabGroupId)
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(groupResponse)
        assertNotNull(groupResponse.body())
    }

    @Test
    @Throws(Exception::class)
    fun getIssues() {
        val defaultState = RuntimeEnvironment.application.resources.getString(R.string.issue_state_value_default)
        val issuesResponse = gitLab!!
                .getIssues(fakeProject!!.id, defaultState)
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(issuesResponse)
        assertNotNull(issuesResponse.body())
    }

    @Test
    @Throws(Exception::class)
    fun getFiles() {
        val defaultBranch = "master"
        val currentPath = ""
        val treeResponse = gitLab!!
                .getTree(fakeProject!!.id, defaultBranch, currentPath)
                .blockingGet()
        assertNotNull(treeResponse)
    }

    @Test
    @Throws(Exception::class)
    fun getCommits() {
        val defaultBranch = "master"
        val commitsResponse = gitLab!!
                .getCommits(fakeProject!!.id, defaultBranch, 1)
                .blockingGet()
        assertNotNull(commitsResponse)
    }

    @Test
    @Throws(Exception::class)
    fun getMergeRequests() {
        val defaultState = RuntimeEnvironment.application.resources.getString(R.string.merge_request_state_value_default)
        val mergeRequestResponse = gitLab!!
                .getMergeRequests(fakeProject!!.id, defaultState)
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(mergeRequestResponse)
        assertNotNull(mergeRequestResponse.body())
    }

    @Test
    @Throws(Exception::class)
    fun getCurrentUser() {
        val userFullResponse = gitLab!!
                .getThisUser()
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(userFullResponse)
        assertNotNull(userFullResponse.body())
    }

    //    @Test
    @Throws(Exception::class)
    fun uploadFile() {
        val bitmap = BitmapFactory.decodeResource(RuntimeEnvironment.application.resources, R.drawable.ic_fork)
        val part = FileUtil.toPart(bitmap, "fork.png")

        val uploadResponseResponse = gitLab!!.uploadFile(fakeProject!!.id, part)
                .blockingGet()
        assertNotNull(uploadResponseResponse)
    }
}