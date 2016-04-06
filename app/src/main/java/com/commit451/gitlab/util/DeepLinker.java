package com.commit451.gitlab.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.commit451.gitlab.R;

/**
 * Generates deeplinks
 */
public class DeepLinker {

    public static final String EXTRA_ORIGINAL_URI = "original_uri";

    public static Intent generateDeeplinkIntentFromUri(Context context, Uri originalUri) {
        Uri uri = originalUri.buildUpon()
                .scheme(context.getString(R.string.deeplink_scheme))
                .build();
        return generatePrivateIntent(context, uri, originalUri);
    }

    private static Intent generatePrivateIntent(Context context, Uri uri, Uri originalUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra(EXTRA_ORIGINAL_URI, originalUri);
        intent.setPackage(context.getPackageName());
        return intent;
    }
}
