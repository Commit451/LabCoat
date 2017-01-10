package com.commit451.gitlab.activity

import android.support.design.widget.TextInputLayout
import android.text.TextUtils

import com.commit451.gitlab.R
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity

/**
 * Base activity for others to derive from
 */
open class BaseActivity : RxAppCompatActivity() {

    fun hasEmptyFields(vararg textInputLayouts: TextInputLayout): Boolean {
        var hasEmptyField = false
        for (textInputLayout in textInputLayouts) {
            if (TextUtils.isEmpty(textInputLayout.editText!!.text)) {
                textInputLayout.error = getString(R.string.required_field)
                hasEmptyField = true
            } else {
                textInputLayout.error = null
            }
        }
        return hasEmptyField
    }
}
