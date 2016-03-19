package com.commit451.gitlab.activity;

import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.afollestad.appthemeengine.ATEActivity;
import com.commit451.gitlab.R;

/**
 * Created by Jawn on 7/27/2015.
 */
public class BaseActivity extends ATEActivity {

    @Nullable
    @Override
    public final String getATEKey() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", true) ?
                "dark_theme" : "light_theme";
    }

    public boolean hasEmptyFields(TextInputLayout... textInputLayouts) {
        boolean hasEmptyField = false;
        for (TextInputLayout textInputLayout : textInputLayouts) {
            if (TextUtils.isEmpty(textInputLayout.getEditText().getText())) {
                textInputLayout.setError(getString(R.string.required_field));
                hasEmptyField = true;
            } else {
                textInputLayout.setError(null);
            }
        }
        return hasEmptyField;
    }
}
