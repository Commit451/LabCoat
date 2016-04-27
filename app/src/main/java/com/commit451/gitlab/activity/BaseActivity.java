package com.commit451.gitlab.activity;

import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.commit451.gitlab.R;

/**
 * Base activity for others to derive from
 */
public class BaseActivity extends AppCompatActivity {

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
