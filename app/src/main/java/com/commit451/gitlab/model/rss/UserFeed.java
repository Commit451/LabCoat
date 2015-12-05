package com.commit451.gitlab.model.rss;

import org.parceler.Parcel;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Parcel
@Root(strict = false)
public class UserFeed {
    @Element(name = "title", required = true)
    String mTitle;
    @ElementList(name = "entry", required = false, inline = true)
    List<FeedEntry> mEntryList;

    public UserFeed() {}

    public String getTitle() {
        return mTitle;
    }

    public List<FeedEntry> getEntries() {
        return mEntryList;
    }
}
