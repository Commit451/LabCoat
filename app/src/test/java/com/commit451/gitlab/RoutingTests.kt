package com.commit451.gitlab

import android.net.Uri
import com.commit451.gitlab.navigation.RoutingRouter
import junit.framework.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

/**
 * Tests account login and basic retrieval stuff
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class RoutingTests {

    companion object {

        @BeforeClass
        @Throws(Exception::class)
        fun setUp() {
            //for logging
            ShadowLog.stream = System.out
        }
    }

    @Test
    fun routeIssues() {
        val countingRouter = CountingRouter()
        val router = RoutingRouter(countingRouter)
        var issueUrl = Uri.parse("https://gitlab.com/Commit451/LabCoat/issues/153")
        router.route(issueUrl)
        Assert.assertEquals(1, countingRouter.issueRouteCount)
        issueUrl = Uri.parse("gitlab.com/Commit451/LabCoat/issues")
        router.route(issueUrl)
        Assert.assertEquals(1, countingRouter.projectRouteCount)
        issueUrl = Uri.parse("http://example.com/wehostourgitlabserverhere/Commit451/LabCoat/issues")
        router.route(issueUrl)
        Assert.assertEquals(2, countingRouter.projectRouteCount)
    }

    @Test
    fun routeCommits() {
        var commitUrl = Uri.parse("https://gitlab.com/Commit451/LabCoat/commit/434fb013607836620819fae09f23a72d88369d3d")
        val countingRouter = CountingRouter()
        val router = RoutingRouter(countingRouter)
        router.route(commitUrl)
        Assert.assertEquals(1, countingRouter.commitRouteCount)
        commitUrl = Uri.parse("http://gitlab.com/Commit451/LabCoat/commits")
        router.route(commitUrl)
        Assert.assertEquals(1, countingRouter.projectRouteCount)
        //Test for subdomain
        commitUrl = Uri.parse("http://example.com/wehostourgitlabserverhere/Commit451/LabCoat/commit/434fb013607836620819fae09f23a72d88369d3d")
        router.route(commitUrl)
        Assert.assertEquals(2, countingRouter.commitRouteCount)
    }

    @Test
    fun routeMergeRequests() {
        var mergeRequestUrl = Uri.parse("https://gitlab.com/Commit451/LabCoat/merge_requests/14")
        val countingRouter = CountingRouter()
        val router = RoutingRouter(countingRouter)
        router.route(mergeRequestUrl)
        Assert.assertEquals(1, countingRouter.mergeRequestRouteCount)
        mergeRequestUrl = Uri.parse("http://gitlab.com/Commit451/LabCoat/commits")
        router.route(mergeRequestUrl)
        Assert.assertEquals(1, countingRouter.projectRouteCount)
        //Test for subdomain
        mergeRequestUrl = Uri.parse("http://example.com/wehostourgitlabserverhere/Commit451/LabCoat/merge_requests/13")
        router.route(mergeRequestUrl)
        Assert.assertEquals(2, countingRouter.mergeRequestRouteCount)
    }
}