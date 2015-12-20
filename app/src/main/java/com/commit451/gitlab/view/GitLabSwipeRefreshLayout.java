package com.commit451.gitlab.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.commit451.gitlab.R;


/**
 * Just so that we do not have to keep setting the colors everywhere
 * Created by Jawn on 7/5/2015.
 */
public class GitLabSwipeRefreshLayout extends SwipeRefreshLayout {

    public GitLabSwipeRefreshLayout(Context context) {
        super(context);
        init();
    }

    public GitLabSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setColorSchemeResources(R.color.red, R.color.orange, R.color.yellow);
    }
}
