package com.commit451.gitlab.extension

import android.view.View
import com.commit451.gitlab.activity.BaseActivity

fun View.baseActivity(): BaseActivity {
    return context as BaseActivity
}
