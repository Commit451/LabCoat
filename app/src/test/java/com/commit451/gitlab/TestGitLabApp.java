package com.commit451.gitlab;

import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;

/**
 * Test version of our Application class, used by Robolectric
 */
public class TestGitLabApp extends GitLabApp implements TestLifecycleApplication {

    @Override
    public void beforeTest(Method method) {

    }

    @Override
    public void prepareTest(Object test) {

    }

    @Override
    public void afterTest(Method method) {

    }

    @Override
    protected void setupCrashReporting() {
        //Intentionally left blank
    }
}
