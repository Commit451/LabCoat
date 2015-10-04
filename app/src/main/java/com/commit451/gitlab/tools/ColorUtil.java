package com.commit451.gitlab.tools;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;

import com.commit451.gitlab.R;

public class ColorUtil {

    private static float[] hsv = new float[3];

    public static int getDarkerColor(int color) {
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static void setStatusBarAndNavBarColor(Window window, int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(color);
            window.setNavigationBarColor(color);
        }
    }

    public static void animateStatusBarAndNavBarColors(Window window, int endColor) {
        if (Build.VERSION.SDK_INT >= 21) {
            statusBar(window, window.getStatusBarColor(), endColor);
            navigationBar(window, window.getNavigationBarColor(), endColor);
        }
    }

    public static ColorStateList createColorStateList(int color) {
        return ColorStateList.valueOf(color);
    }

    public static ColorStateList createColorStateList(int color, int pressed) {
        return new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{}
        }, new int[]{
                pressed,
                color
        });
    }

    public static void setBackgroundDrawable(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    public static int getBackgroundColor(View v) {
        Drawable background = v.getBackground();
        if (background instanceof ColorDrawable) {
            return ((ColorDrawable) background).getColor();
        } else {
            return Color.TRANSPARENT;
        }
    }

    public static Animator animateBackgroundColor(View v, int endColor) {
        ObjectAnimator oa = ObjectAnimator.ofObject(v, "backgroundColor", new ArgbEvaluator(),
                getBackgroundColor(v), endColor);
        oa.start();
        return oa;
    }

    public static Animator statusBar(Window window, int startColor, int endColor) {
        ObjectAnimator oa = ObjectAnimator.ofObject(window, "statusBarColor", new ArgbEvaluator(),
                startColor, endColor);
        oa.start();
        return oa;
    }

    public static Animator navigationBar(Window window, int startColor, int endColor) {
        ObjectAnimator oa = ObjectAnimator.ofObject(window, "navigationBarColor", new ArgbEvaluator(),
                startColor, endColor);
        oa.start();
        return oa;
    }

    public static void setTint(CheckBox box, int color, int unpressedColor) {
        ColorStateList sl = new ColorStateList(new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}
        }, new int[]{
                unpressedColor,
                color
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            box.setButtonTintList(sl);
        } else {
            Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(box.getContext(), R.drawable.abc_btn_check_material));
            DrawableCompat.setTintList(drawable, sl);
            box.setButtonDrawable(drawable);
        }
    }

    public static void setTint(SwitchCompat switchCompat, int color, int unpressedColor) {
        ColorStateList sl = new ColorStateList(new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}
        }, new int[]{
                unpressedColor,
                color
        });
        DrawableCompat.setTintList(switchCompat.getThumbDrawable(), sl);
    }

    public static void setMenuItemsColor(Menu menu, int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable icon = menu.getItem(i).getIcon();
            if (icon != null) {
                icon.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    public static Animator animateTextColor(TextView textView, int color) {
        ObjectAnimator oa = ObjectAnimator.ofObject(textView, "textColor", new ArgbEvaluator(),
                textView.getCurrentTextColor(), color);
        oa.start();
        return oa;
    }

}
