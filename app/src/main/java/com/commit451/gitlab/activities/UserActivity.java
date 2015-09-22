package com.commit451.gitlab.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.User;

import butterknife.ButterKnife;

/**
 * User activity, which shows the user!
 * Created by Jawn on 9/21/2015.
 */
public class UserActivity extends BaseActivity {

    public static Intent newInstance(Context context, User user) {
        Intent intent = new Intent(context, UserActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
    }
}
