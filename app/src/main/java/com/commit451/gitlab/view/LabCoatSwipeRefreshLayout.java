package com.commit451.gitlab.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.commit451.gitlab.R;


/**
 * Just so that we do not have to keep setting the colors everywhere
 */
public class LabCoatSwipeRefreshLayout extends SwipeRefreshLayout {

    public LabCoatSwipeRefreshLayout(Context context) {
        super(context);
        init();
    }

    public LabCoatSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        int[] colors = getResources().getIntArray(R.array.cool_colors);
        setColorSchemeColors(colors);
    }
}
