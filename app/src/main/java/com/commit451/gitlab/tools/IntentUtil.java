package com.commit451.gitlab.tools;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.commit451.gitlab.R;

/**
 * Created by Jawn on 8/25/2015.
 */
public class IntentUtil {

    public static void openPage(View root, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        try {
            root.getContext().startActivity(i);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(root, R.string.error_no_browser, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    public static void share(View root, String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        root.getContext().startActivity(Intent.createChooser(shareIntent, root.getContext().getString(R.string.action_share)));
        try {
            root.getContext().startActivity(shareIntent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(root, R.string.error_no_browser, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }
}
