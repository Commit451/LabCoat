package com.commit451.gitlab.util

import android.support.design.widget.TextInputLayout
import android.text.TextUtils

/**
 * Validates input
 */
object Validator {

    /**
     * Make sure all the edittexts are not empty and fill in an error message if they are

     * @param textInputLayouts all the input layouts you wish to validate
     * *
     * @return true if all the fields were valid.
     */
    fun validateFieldsNotEmpty(errorText: String, vararg textInputLayouts: TextInputLayout): Boolean {
        var valid = true
        for (textInputLayout in textInputLayouts) {
            if (textInputLayout.editText != null && TextUtils.isEmpty(textInputLayout.editText!!.text.toString().trim { it <= ' ' })) {
                textInputLayout.error = errorText
                valid = false
            } else {
                //clear out a possible previous error
                textInputLayout.error = null
            }
        }
        return valid
    }
}
