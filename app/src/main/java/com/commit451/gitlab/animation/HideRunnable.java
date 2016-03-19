package com.commit451.gitlab.animation;

import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Hides the view when run. Usually used with {@link android.view.ViewPropertyAnimator#withEndAction(Runnable)}
 */
public class HideRunnable implements Runnable {

    private WeakReference<View> mViewWeakReference;

    public HideRunnable(View view) {
        mViewWeakReference = new WeakReference<>(view);
    }

    @Override
    public void run() {
        if (mViewWeakReference != null) {
            View view = mViewWeakReference.get();
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
    }
}
