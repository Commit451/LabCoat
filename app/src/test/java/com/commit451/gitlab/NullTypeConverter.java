package com.commit451.gitlab;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

/**
 * The worst type converter you will ever see. Only used in testing if Robolectric decides to break
 * and not know what a {@link android.net.Uri} or other class is
 */
public class NullTypeConverter extends StringBasedTypeConverter {

    @Override
    public String convertToString(Object object) {
        return null;
    }

    @Override
    public Object getFromString(String string) {
        return null;
    }
}
