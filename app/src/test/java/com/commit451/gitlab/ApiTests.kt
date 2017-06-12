package com.commit451.gitlab

import com.commit451.gitlab.api.GitLabService
import com.commit451.gitlab.model.api.Project
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Test


/**
 * Tests account login and basic retrieval stuff
 */
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
        val defaultState = "opened"
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
        val defaultState = "opened"
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
}