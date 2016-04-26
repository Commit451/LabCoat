package com.commit451.gitlab.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.appthemeengine.Config;
import com.commit451.gitlab.R;
import com.commit451.gitlab.navigation.BrowserFallback;
import com.commit451.gitlab.navigation.LabCoatIntentCustomizer;
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs;

/**
 * All the things to do with intents
 */
public class IntentUtil {

    public static void openPage(Activity activity, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        int primaryColor = Config.primaryColor(activity, AppThemeUtil.resolveThemeKey(activity));
        SimpleChromeCustomTabs.getInstance()
                .withFallback(new BrowserFallback(activity))
                .withIntentCustomizer(new LabCoatIntentCustomizer(activity, primaryColor))
                .navigateTo(Uri.parse(url), activity);
    }

    public static void share(View root, Uri url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url.toString());
        try {
            root.getContext().startActivity(shareIntent);
        } catch (Exception e) {
            Snackbar.make(root, R.string.error_could_not_share, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }
}
