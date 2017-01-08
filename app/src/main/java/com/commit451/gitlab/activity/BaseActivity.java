package com.commit451.gitlab.activity;

import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;

import com.commit451.gitlab.R;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

/**
 * Base activity for others to derive from
 */
public class BaseActivity extends RxAppCompatActivity {

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
