package com.commit451.gitlab.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.commit451.gitlab.R;import com.novoda.simplechromecustomtabs.navigation.NavigationFallback;

/**
 * A fallback to open the url in the browser
 */
public class BrowserFallback implements NavigationFallback {

    private Context mContext;

    public BrowserFallback(Context context) {
        mContext = context;
    }

    @Override
    public void onFallbackNavigateTo(Uri url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(url);
        try {
            mContext.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(mContext, R.string.error_no_browser, Toast.LENGTH_SHORT)
                    .show();
        }
    }
}