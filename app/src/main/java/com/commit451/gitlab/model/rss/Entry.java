package com.commit451.gitlab.model.rss;

import org.parceler.Parcel;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Date;

@Parcel
@Root(strict = false)
public class Entry {
    @Element(name = "link", required = true)
    Link link;
    @Element(name = "title", required = true)
    String title;
    @Element(name = "updated", required = true)
    Date updated;
    @Element(name = "thumbnail", required = true)
    Thumbnail thumbnail;
    @Element(name = "summary", required = true)
    String summary;

    public Entry() {}

    public Link getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public Date getUpdated() {
        return updated;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public String getSummary() {
        return summary;
    }
}
