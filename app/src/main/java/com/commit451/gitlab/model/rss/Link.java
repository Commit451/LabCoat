package com.commit451.gitlab.model.rss;

import org.parceler.Parcel;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Link!
 * Created by John on 10/8/15.
 */
@Parcel
@Root(strict = false)
public class Link {

    @Attribute(name = "href")
    String mHref;

    public Link() {
    }

    public String getHref() {
        return mHref;
    }
}
