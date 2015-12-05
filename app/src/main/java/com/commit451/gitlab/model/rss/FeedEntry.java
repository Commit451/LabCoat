package com.commit451.gitlab.model.rss;

import org.parceler.Parcel;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Date;

@Parcel
@Root(strict = false)
public class FeedEntry {
    @Element(name = "link", required = true)
    Link mLink;
    @Element(name = "title", required = true)
    String mTitle;
    @Element(name = "summary", required = true)
    String mSummary;
    @Element(name = "updated", required = true)
    Date mUpdated;
    @Element(name = "thumbnail", required = true)
    Thumbnail mThumbnail;

    public FeedEntry() {}

    public Link getLink() {
        return mLink;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSummary() {
        return mSummary;
    }

    public Date getUpdated() {
        return mUpdated;
    }

    public Thumbnail getThumbnail() {
        return mThumbnail;
    }
}
