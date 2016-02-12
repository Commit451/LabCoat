package com.commit451.gitlab.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.ArcMotion;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;
import com.commit451.gitlab.transition.MorphDialogToFab;
import com.commit451.gitlab.transition.MorphFabToDialog;

/**
 * Activity that morphs from a FAB. Make sure the view you want to morph has the view id R.id.mRoot and
 * call {@link #morph(View)} when the content view is set
 */
public class MorphActivity extends BaseActivity {


    protected void morph(View root) {
        if (root == null) {
            throw new IllegalStateException("Cannot pass an empty view");
        }
        if (Build.VERSION.SDK_INT >= 21) {
            int fabColor = Easel.getThemeAttrColor(this, R.attr.colorAccent);
            int dialogColor = Easel.getThemeAttrColor(this, android.R.attr.windowBackground);
            setupSharedElementTransitionsFab(this, root,
                    fabColor,
                    dialogColor,
                    getResources().getDimensionPixelSize(R.dimen.dialog_corners));
        }
    }

    @TargetApi(21)
    public void setupSharedElementTransitionsFab(@NonNull Activity activity,
                                                 @Nullable View target,
                                                 int fabColor,
                                                 int dialogColor,
                                                 int dialogCornerRadius) {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(50f);
        arcMotion.setMinimumVerticalAngle(50f);
        Interpolator easeInOut = AnimationUtils.loadInterpolator(activity, android.R.interpolator.fast_out_slow_in);
        MorphFabToDialog sharedEnter = new MorphFabToDialog(fabColor, dialogColor, dialogCornerRadius);
        sharedEnter.setPathMotion(arcMotion);
        sharedEnter.setInterpolator(easeInOut);
        MorphDialogToFab sharedReturn = new MorphDialogToFab(dialogColor, fabColor);
        sharedReturn.setPathMotion(arcMotion);
        sharedReturn.setInterpolator(easeInOut);
        if (target != null) {
            sharedEnter.addTarget(target);
            sharedReturn.addTarget(target);
        }
        activity.getWindow().setSharedElementEnterTransition(sharedEnter);
        activity.getWindow().setSharedElementReturnTransition(sharedReturn);
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
