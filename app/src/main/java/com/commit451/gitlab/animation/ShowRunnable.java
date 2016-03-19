package com.commit451.gitlab.animation;

import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Shows the view. Usually used with {@link android.view.ViewPropertyAnimator#withStartAction(Runnable)}
 */
public class ShowRunnable implements Runnable {

    private WeakReference<View> mViewWeakReference;

    public ShowRunnable(View view) {
        mViewWeakReference = new WeakReference<>(view);
    }

    @Override
    public void run() {
        if (mViewWeakReference != null) {
            View view = mViewWeakReference.get();
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }
}
