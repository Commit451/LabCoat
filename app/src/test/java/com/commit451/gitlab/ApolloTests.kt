package com.commit451.gitlab

import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.model.api.Project
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class ApolloTests {

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
            Assert.assertNotNull(projectResponse)

            fakeProject = projectResponse
        }
    }

    @Test
    fun currentUser() {
        val currentUser = gitLab
                .currentUser()
                .blockingGet()
        Assert.assertNotNull(currentUser?.avatarUrl)
    }
}
