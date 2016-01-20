package com.commit451.gitlab.customtabs;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;

import com.commit451.gitlab.R;


/**
 * A fallback to open the url in the browser
 * Created by John on 9/3/15.
 */
public class BrowserFallback implements CustomTabsActivityHelper.CustomTabFallback {

    @Override
    public void openUri(Activity activity, Uri uri) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(uri);
        try {
            activity.startActivity(i);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(activity.getWindow().getDecorView(), R.string.error_no_browser, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }
}
