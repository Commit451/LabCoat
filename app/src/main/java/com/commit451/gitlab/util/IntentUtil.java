package com.commit451.gitlab.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;
import com.commit451.gitlab.customtabs.BrowserFallback;
import com.commit451.gitlab.customtabs.CustomTabsActivityHelper;

/**
 * All the things to do with intents
 * Created by Jawn on 8/25/2015.
 */
public class IntentUtil {

    public static void openPage(Activity activity, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setToolbarColor(Easel.getThemeAttrColor(activity, R.attr.colorPrimary));
        intentBuilder.setStartAnimations(activity, R.anim.fade_in, R.anim.do_nothing);
        intentBuilder.setExitAnimations(activity, R.anim.do_nothing, R.anim.fade_out);
        CustomTabsActivityHelper.openCustomTab(activity, intentBuilder.build(), Uri.parse(url), new BrowserFallback());
    }

    public static void share(View root, Uri url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url.toString());
        try {
            root.getContext().startActivity(shareIntent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(root, R.string.error_could_not_share, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }
}
