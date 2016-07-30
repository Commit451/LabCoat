package com.commit451.gitlab.navigation;

import android.app.Activity;

import com.commit451.gitlab.R;
import com.novoda.simplechromecustomtabs.navigation.IntentCustomizer;
import com.novoda.simplechromecustomtabs.navigation.SimpleChromeCustomTabsIntentBuilder;

import java.lang.ref.WeakReference;

/**
 * Customize custom intents
 */
public class LabCoatIntentCustomizer implements IntentCustomizer {

    private WeakReference<Activity> mActivity;
    private int mToolbarColor;

    public LabCoatIntentCustomizer(Activity activity, int toolbarColor) {
        mActivity = new WeakReference<>(activity);
        mToolbarColor = toolbarColor;
    }

    @Override
    public SimpleChromeCustomTabsIntentBuilder onCustomiseIntent(SimpleChromeCustomTabsIntentBuilder simpleChromeCustomTabsIntentBuilder) {
        Activity activity = mActivity.get();
        if (activity == null) {
            return simpleChromeCustomTabsIntentBuilder;
        }
        return simpleChromeCustomTabsIntentBuilder
                .withStartAnimations(activity, R.anim.fade_in, R.anim.do_nothing)
                .withExitAnimations(activity, R.anim.do_nothing, R.anim.fade_out)
                .withToolbarColor(mToolbarColor);
    }
}
