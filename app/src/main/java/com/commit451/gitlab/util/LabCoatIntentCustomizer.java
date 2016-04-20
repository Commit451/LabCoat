package com.commit451.gitlab.util;

import android.app.Activity;

import com.commit451.gitlab.R;
import com.novoda.simplechromecustomtabs.navigation.IntentCustomizer;
import com.novoda.simplechromecustomtabs.navigation.SimpleChromeCustomTabsIntentBuilder;

/**
 * Customize custom intents
 * <br>
 * Copyright 2016 <a href="http://www.ovenbits.com">Oven Bits</a>
 *
 * @author Jawn.
 */
public class LabCoatIntentCustomizer implements IntentCustomizer {

    private Activity mActivity;
    private int mToolbarColor;

    public LabCoatIntentCustomizer(Activity activity, int toolbarColor) {
        mActivity = activity;
        mToolbarColor = toolbarColor;
    }

    @Override
    public SimpleChromeCustomTabsIntentBuilder onCustomiseIntent(SimpleChromeCustomTabsIntentBuilder simpleChromeCustomTabsIntentBuilder) {
        return simpleChromeCustomTabsIntentBuilder
                .withStartAnimations(mActivity, R.anim.fade_in, R.anim.do_nothing)
                .withExitAnimations(mActivity, R.anim.do_nothing, R.anim.fade_out)
                .withToolbarColor(mToolbarColor);
    }
}
