package com.commit451.gitlab.activity

import android.support.design.widget.TextInputLayout
import android.text.TextUtils

import com.commit451.gitlab.R
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity

/**
 * Base activity for others to derive from
 */
open class BaseActivity : RxAppCompatActivity() {

    open fun hasBrowsableLinks(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        if (hasBrowsableLinks()) {
            SimpleChromeCustomTabs.getInstance().connectTo(this)
        }
    }

    override fun onPause() {
        if (hasBrowsableLinks() && SimpleChromeCustomTabs.getInstance().isConnected) {
            SimpleChromeCustomTabs.getInstance().disconnectFrom(this)
        }
        super.onPause()
    }

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
