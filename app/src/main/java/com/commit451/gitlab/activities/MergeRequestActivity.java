package com.commit451.gitlab.activities;

import android.os.Bundle;

import com.commit451.gitlab.R;

import butterknife.ButterKnife;

/**
 * Shows the details of a merge request
 * Created by John on 11/16/15.
 */
public class MergeRequestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_request);
        ButterKnife.bind(this);

    }
}
