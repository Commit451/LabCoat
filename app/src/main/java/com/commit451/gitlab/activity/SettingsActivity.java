package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.ATEColorPreference;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.commit451.gitlab.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Settings screens are fun!
 * Created by John on 11/10/15.
 */
public class SettingsActivity extends BaseActivity
        implements ColorChooserDialog.ColorCallback {

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        return intent;
    }

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.toolbar_title) TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbarTitle.setText(R.string.settings);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.settings_container, new SettingsFragment()).commit();
        } else {
            SettingsFragment frag = (SettingsFragment) getFragmentManager().findFragmentById(R.id.settings_container);
            if (frag != null) frag.invalidateSettings();
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        final Config config = ATE.config(this, getATEKey());
        switch (dialog.getTitle()) {
            case R.string.primary_color:
                config.primaryColor(selectedColor);
                break;
            case R.string.accent_color:
                config.accentColor(selectedColor);
                // We've overridden the navigation view selected colors in the default config,
                // which means we are responsible for keeping those colors up to date.
                //config.navigationViewSelectedIcon(selectedColor);
                //config.navigationViewSelectedText(selectedColor);
                break;
        }
        config.commit();
        recreate(); // recreation needed to reach the checkboxes in the preferences layout
    }

    public static class SettingsFragment extends PreferenceFragment {

        String mAteKey;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            invalidateSettings();
        }

        public void invalidateSettings() {
            mAteKey = ((SettingsActivity) getActivity()).getATEKey();

            ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
            primaryColorPref.setColor(Config.primaryColor(getActivity(), mAteKey), Color.BLACK);
            primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.primary_color)
                            .preselect(Config.primaryColor(getActivity(), mAteKey))
                            .show();
                    return true;
                }
            });

            ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
            accentColorPref.setColor(Config.accentColor(getActivity(), mAteKey), Color.BLACK);
            accentColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.accent_color)
                            .preselect(Config.accentColor(getActivity(), mAteKey))
                            .show();
                    return true;
                }
            });

            findPreference("dark_theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Marks both theme configs as changed so MainActivity restarts itself on return
                    Config.markChanged(getActivity(), "light_theme");
                    Config.markChanged(getActivity(), "dark_theme");
                    // The dark_theme preference value gets saved by Android in the default PreferenceManager.
                    // It's used in getATEKey() of both the Activities.
                    getActivity().recreate();
                    return true;
                }
            });
        }
    }
}
