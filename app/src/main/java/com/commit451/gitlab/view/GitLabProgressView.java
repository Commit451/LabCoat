package com.commit451.gitlab.view;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.afollestad.appthemeengine.Config;
import com.commit451.gitlab.util.AppThemeUtil;
import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * A subclass of ProgressWheel that automagically themes itself to the accent color
 */
public class GitLabProgressView extends ProgressWheel {

    public GitLabProgressView(Context context) {
        super(context);
        init();
    }

    public GitLabProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBarColor(Config.accentColor(getContext(), AppThemeUtil.resolveThemeKey(getContext())));
    }
}
