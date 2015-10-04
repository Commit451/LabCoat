package com.commit451.gitlab.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.commit451.gitlab.R;

import butterknife.ButterKnife;

/**
 * Displays the groups of the current user
 * Created by Jawn on 10/4/2015.
 */
public class GroupsActivity extends BaseActivity {

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, GroupsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);
    }
}
