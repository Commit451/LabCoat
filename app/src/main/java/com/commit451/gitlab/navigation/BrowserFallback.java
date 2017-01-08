package com.commit451.gitlab.navigation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.commit451.gitlab.R;
import com.novoda.simplechromecustomtabs.navigation.NavigationFallback;

import java.lang.ref.WeakReference;

/**
 * A fallback to open the url in the browser
 */
public class BrowserFallback implements NavigationFallback {

    private WeakReference<Context> context;

    public BrowserFallback(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    public void onFallbackNavigateTo(Uri url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(url);
        Context context = this.context.get();
        if (context == null) {
            return;
        }
        try {
            context.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(context, R.string.error_no_browser, Toast.LENGTH_SHORT)
                    .show();
        }
    }
}