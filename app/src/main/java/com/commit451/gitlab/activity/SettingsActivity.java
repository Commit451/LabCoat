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
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.data.Prefs;

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
    @BindView(R.id.text_launch_activity)
    TextView textLaunchActivity;
    @BindView(R.id.root_require_device_auth)
    ViewGroup rootRequireDeviceAuth;
    @BindView(R.id.switch_require_auth)
    SwitchCompat switchRequireAuth;

    @OnClick(R.id.root_launch_activity)
    void onLaunchActivityClicked() {
        new MaterialDialog.Builder(this)
                .title(R.string.setting_starting_view)
                .items(R.array.setting_starting_view_choices)
                .itemsCallbackSingleChoice(getSelectedIndex(), new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        App.get().getPrefs().setStartingView(which);
                        bindPrefs();
                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        return true;
                    }
                })
                .show();
    }

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
        setStartingViewSelection();
        switchRequireAuth.setChecked(App.get().getPrefs().isRequireDeviceAuth());
    }

    private void setStartingViewSelection() {
        int startinView = App.get().getPrefs().getStartingView();
        switch (startinView) {
            case Prefs.STARTING_VIEW_PROJECTS:
                textLaunchActivity.setText(R.string.setting_starting_view_projects);
                break;
            case Prefs.STARTING_VIEW_GROUPS:
                textLaunchActivity.setText(R.string.setting_starting_view_groups);
                break;
            case Prefs.STARTING_VIEW_ACTIVITY:
                textLaunchActivity.setText(R.string.setting_starting_view_activity);
                break;
            case Prefs.STARTING_VIEW_TODOS:
                textLaunchActivity.setText(R.string.setting_starting_view_todos);
                break;

        }
    }

    private int getSelectedIndex() {
        return App.get().getPrefs().getStartingView();
    }
}
