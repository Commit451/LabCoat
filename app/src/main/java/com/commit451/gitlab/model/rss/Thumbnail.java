package com.commit451.gitlab.model.rss;

import org.parceler.Parcel;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import android.net.Uri;

@Parcel
@Root(strict = false)
public class Thumbnail {
    @Attribute(name = "url", required = true)
    Uri mUrl;

    public Thumbnail() {}

    public Uri getUrl() {
        return mUrl;
    }
}
