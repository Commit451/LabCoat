package com.commit451.gitlab.extension

import com.google.android.material.textfield.TextInputLayout
import com.commit451.gitlab.R

fun TextInputLayout.checkValid(): Boolean {
    if (editText!!.text.isNullOrEmpty()) {
        error = resources.getString(R.string.required_field)
        return false
    } else {
        error = null
        return true
    }
}

fun TextInputLayout.text(): String {
    return editText!!.text.toString()
}
