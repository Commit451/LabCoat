package com.commit451.gitlab.dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.ArcMotion;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.commit451.gitlab.R;
import com.commit451.gitlab.activities.BaseActivity;
import com.commit451.gitlab.transitions.MorphDialogToFab;
import com.commit451.gitlab.transitions.MorphFabToDialog;

public class PopupActivity extends BaseActivity {

    private static final String KEY_FAB_COLOR = "fab_color";
    private static final String KEY_DIALOG_COLOR = "dialog_color";

    boolean isDismissing = false;
    private ViewGroup container;

    public static Intent getStartIntent(Context context, int fabColor, int dialogColor) {
        Intent intent = new Intent(context, PopupActivity.class);
        intent.putExtra(KEY_FAB_COLOR, fabColor);
        intent.putExtra(KEY_DIALOG_COLOR, dialogColor);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        int fabColor = getIntent().getIntExtra(KEY_FAB_COLOR, Color.WHITE);
        int dialogColor = getIntent().getIntExtra(KEY_DIALOG_COLOR, Color.WHITE);
        if (Build.VERSION.SDK_INT >= 21) {
            setupSharedElementTransitionsFab(this, container,
                    fabColor,
                    dialogColor,
                    getResources().getDimensionPixelSize(R.dimen.dialog_corners));
        }
        container = (ViewGroup) findViewById(R.id.container);
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
        Interpolator easeInOut =
                AnimationUtils.loadInterpolator(activity, android.R.interpolator.fast_out_slow_in);
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @TargetApi(21)
    public void dismiss(View view) {
        isDismissing = true;
        setResult(Activity.RESULT_CANCELED);
        if (Build.VERSION.SDK_INT >= 21) {
            finishAfterTransition();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        dismiss(null);
    }

}