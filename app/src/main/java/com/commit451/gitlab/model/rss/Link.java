package com.commit451.gitlab.model.rss;

import android.net.Uri;

import org.parceler.Parcel;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Parcel
@Root(strict = false)
public class Link {
    @Attribute(name = "href", required = true)
    Uri href;

    public Link() {}

    public Uri getHref() {
        return href;
    }
}
