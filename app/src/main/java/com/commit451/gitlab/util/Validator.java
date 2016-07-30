package com.commit451.gitlab.util;

import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;

/**
 * Validates input
 */
public class Validator {

    /**
     * Make sure all the edittexts are not empty and fill in an error message if they are
     *
     * @param textInputLayouts all the input layouts you wish to validate
     * @return true if all the fields were valid.
     */
    public static boolean validateFieldsNotEmpty(String errorText, TextInputLayout... textInputLayouts) {
        boolean valid = true;
        for (TextInputLayout textInputLayout : textInputLayouts) {
            if (textInputLayout.getEditText() != null && TextUtils.isEmpty(textInputLayout.getEditText().getText().toString().trim())) {
                textInputLayout.setError(errorText);
                valid = false;
            } else {
                //clear out a possible previous error
                textInputLayout.setError(null);
            }
        }
        return valid;
    }
}
