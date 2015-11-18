package com.commit451.gitlab.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.commit451.gitlab.R;

import butterknife.ButterKnife;

/**
 * Settings screens are fun!
 * Created by John on 11/10/15.
 */
public class SettingsActivity extends BaseActivity {

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
    }
}
