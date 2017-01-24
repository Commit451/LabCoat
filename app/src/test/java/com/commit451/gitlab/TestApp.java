package com.commit451.gitlab;

import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;

/**
 * Test version of our Application class, used by Robolectric
 */
public class TestApp extends App implements TestLifecycleApplication {

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

    @Override
    protected void setupLeakCanary() {
        //Intentionally left blank
    }

    @Override
    protected void setupMultidex() {
        //Intentionally left blank
    }
}
