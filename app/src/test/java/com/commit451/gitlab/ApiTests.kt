package com.commit451.gitlab

import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.api.GitLabService
import com.commit451.gitlab.model.api.Project
import com.commit451.gitlab.model.api.RepositoryCommit
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Test


/**
 * Tests account login and basic retrieval stuff
 */
class ApiTests {

    companion object {

        private const val PROJECT_ID: Long = 376651

        private var fakeProject: Project? = null
        private lateinit var gitLab: GitLab

        @JvmStatic
        @BeforeClass
        fun setUp() {
            //for logging

            gitLab = TestUtil.login()

            val projectResponse = gitLab
                    .getProject(PROJECT_ID.toString())
                    .blockingGet()
            assertNotNull(projectResponse)

            fakeProject = projectResponse
        }
    }

    @Test
    fun getProjects() {
        val projectsResponse = gitLab
                .getAllProjects()
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(projectsResponse)
        assertNotNull(projectsResponse.body())
    }

    @Test
    fun getGroups() {
        val groupResponse = gitLab
                .getGroups()
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(groupResponse)
        assertNotNull(groupResponse.body())
    }

    @Test
    fun getGroupMembers() {
        //GitLabService group id
        val gitLabGroupId: Long = 9970
        val groupResponse = gitLab
                .getGroupMembers(gitLabGroupId)
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(groupResponse)
        assertNotNull(groupResponse.body())
    }

    @Test
    fun getIssues() {
        val defaultState = "opened"
        val issuesResponse = gitLab
                .getIssues(fakeProject!!.id, defaultState)
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(issuesResponse)
        assertNotNull(issuesResponse.body())
    }

    @Test
    fun getFiles() {
        val defaultBranch = "master"
        val currentPath = ""
        val treeResponse = gitLab
                .getTree(fakeProject!!.id, defaultBranch, currentPath)
                .blockingGet()
        assertNotNull(treeResponse)
    }

    @Test
    fun getCommits() {
        val defaultBranch = "master"
        val commitsResponse = gitLab
                .getCommits(fakeProject!!.id, defaultBranch)
                .blockingGet()
        assertNotNull(commitsResponse.body())
    }

    @Test
    fun getMergeRequests() {
        val defaultState = "opened"
        val mergeRequestResponse = gitLab
                .getMergeRequests(fakeProject!!.id, defaultState)
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(mergeRequestResponse)
        assertNotNull(mergeRequestResponse.body())
    }

    @Test
    fun getCurrentUser() {
        val userFullResponse = gitLab
                .getThisUser()
                .blockingGet()
        TestUtil.assertRetrofitResponseSuccess(userFullResponse)
        assertNotNull(userFullResponse.body())
    }

    @Test
    fun loadAnyList() {
        val url = "https://gitlab.com/api/v4/projects/473568/repository/commits?id=473568&order=default&page=2&per_page=20"
        val result = gitLab.loadAnyList<RepositoryCommit>(url)
                .blockingGet()
        val commit = result.body()?.first()
        assertNotNull(commit)
    }
}