package com.commit451.gitlab.api.converter;

import android.net.Uri;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

/**
 * Simple Uri type converter
 */
public class UriTypeConverter extends StringBasedTypeConverter<Uri> {

    @Override
    public String convertToString(Uri object) {
        return object.toString();
    }

    @Override
    public Uri getFromString(String string) {
        return Uri.parse(string);
    }
}
