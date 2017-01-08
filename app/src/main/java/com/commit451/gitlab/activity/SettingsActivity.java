package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Settings
 */
public class SettingsActivity extends BaseActivity {

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        return intent;
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.root_require_device_auth)
    ViewGroup rootRequireDeviceAuth;
    @BindView(R.id.switch_require_auth)
    SwitchCompat switchRequireAuth;

    @OnClick(R.id.root_require_device_auth)
    void onRequireDeviceAuthClicked() {
        switchRequireAuth.toggle();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.settings);
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (Build.VERSION.SDK_INT < 21) {
            //lollipop+ only!
            rootRequireDeviceAuth.setVisibility(View.GONE);
        }

        bindPrefs();
        switchRequireAuth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                App.get().getPrefs().setRequireDeviceAuth(b);
            }
        });
    }

    private void bindPrefs() {
        switchRequireAuth.setChecked(App.get().getPrefs().isRequireDeviceAuth());
    }
}
