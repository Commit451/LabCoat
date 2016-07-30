package com.commit451.gitlab.view;

import android.content.Context;
import android.util.AttributeSet;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;
import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * A subclass of ProgressWheel that automagically themes itself to the accent color
 */
public class LabCoatProgressView extends ProgressWheel {

    public LabCoatProgressView(Context context) {
        super(context);
        init();
    }

    public LabCoatProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBarColor(Easel.getThemeAttrColor(getContext(), R.attr.colorAccent));
    }
}
