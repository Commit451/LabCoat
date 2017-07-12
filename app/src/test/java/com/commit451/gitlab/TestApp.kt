package com.commit451.gitlab

import org.robolectric.TestLifecycleApplication

import java.lang.reflect.Method

/**
 * Test version of our Application class, used by Robolectric
 */
class TestApp : App(), TestLifecycleApplication {

    override fun beforeTest(method: Method) {

    }

    override fun prepareTest(test: Any) {

    }

    override fun afterTest(method: Method) {

    }

    override fun setupCrashReporting() {
        //Intentionally left blank
    }

    override fun setupLeakCanary() {
        //Intentionally left blank
    }

    override fun setupMultidex() {
        //Intentionally left blank
    }

    override fun setupThreeTen() {
        //Intentionally left blank
    }
}
