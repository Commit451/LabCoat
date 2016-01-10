package com.commit451.gitlab.model.rss;

import org.parceler.Parcel;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import android.net.Uri;

@Parcel
@Root(strict = false)
public class Link {
    @Attribute(name = "href", required = true)
    Uri mHref;

    public Link() {}

    public Uri getHref() {
        return mHref;
    }
}
