package com.commit451.gitlab.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;


/**
 * A subclass of ProgressWheel that automagically themes itself to the accent color
 */
public class LabCoatProgressView extends MaterialProgressBar {

    public LabCoatProgressView(Context context) {
        super(context);
        init();
    }

    public LabCoatProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        int color = Easel.getThemeAttrColor(getContext(), R.attr.colorAccent);
        setProgressTintList(ColorStateList.valueOf(color));
    }
}
