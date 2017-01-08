package com.commit451.gitlab.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

import com.commit451.morphtransitions.FabTransform;

/**
 * Activity that morphs from a FAB. Make sure the view you want to morph has the view id R.id.root and
 * call {@link #morph(View)} when the content view is set
 */
public class MorphActivity extends BaseActivity {

    protected void morph(View root) {
        if (root == null) {
            throw new IllegalStateException("Cannot pass an empty view");
        }
        FabTransform.setup(this, root);
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    @TargetApi(21)
    public void dismiss() {
        if (Build.VERSION.SDK_INT >= 21) {
            finishAfterTransition();
        } else {
            finish();
        }
    }
}
