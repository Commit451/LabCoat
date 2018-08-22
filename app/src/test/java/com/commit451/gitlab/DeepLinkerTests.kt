package com.commit451.gitlab

import com.commit451.gitlab.navigation.DeepLinker
import org.junit.Assert
import org.junit.Test

/**
 * Tests deeplink routing
 */
class DeepLinkerTests {

    @Test
    fun issuesTest() {
        val link = "https://gitlab.com/Commit451/LabCoat/issues"
        val callbacks = CounterCallbacks()
        DeepLinker.route(link, callbacks)
        Assert.assertEquals(1, callbacks.project)
    }

    @Test
    fun projectTest() {
        val link = "https://gitlab.com/Commit451/LabCoat"
        val callbacks = CounterCallbacks()
        DeepLinker.route(link, callbacks)
        Assert.assertEquals(1, callbacks.project)
    }

    @Test
    fun commits() {
        val link = "https://gitlab.com/Commit451/LabCoat/commits"
        val callbacks = CounterCallbacks()
        DeepLinker.route(link, callbacks)
        Assert.assertEquals(1, callbacks.project)
    }

    @Test
    fun builds() {
        val link = "https://gitlab.com/Commit451/LabCoat/builds/artifacts/master/browse"
        val callbacks = CounterCallbacks()
        DeepLinker.route(link, callbacks)
        Assert.assertEquals(1, callbacks.build)
    }

    @Test
    fun issuesInternalTest() {
        val link = "labcoat://gitlab.com/Commit451/LabCoat/issues/392"
        val callbacks = CounterCallbacks()
        DeepLinker.route(link, callbacks)
        Assert.assertEquals(1, callbacks.issue)
    }
}