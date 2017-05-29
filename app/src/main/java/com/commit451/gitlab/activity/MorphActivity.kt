package com.commit451.gitlab.activity

import android.os.Build
import android.view.View
import com.commit451.morphtransitions.FabTransform

/**
 * Activity that morphs from a FAB. Make sure the view you want to morph has the view id R.id.root and
 * call [.morph] when the content view is set. Does nothing if not on 21+
 */
open class MorphActivity : BaseActivity() {

    protected fun morph(root: View?) {
        if (root == null) {
            throw IllegalStateException("Cannot pass an empty view")
        }
        if (Build.VERSION.SDK_INT >= 21) {
            FabTransform.setup(this, root)
        }
    }

    override fun onBackPressed() {
        dismiss()
    }

    fun dismiss() {
        if (Build.VERSION.SDK_INT >= 21) {
            finishAfterTransition()
        } else {
            finish()
        }
    }
}
