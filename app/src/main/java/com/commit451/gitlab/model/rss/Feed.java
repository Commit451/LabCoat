package com.commit451.gitlab.model.rss;

import org.parceler.Parcel;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Parcel
@Root(strict = false)
public class Feed {
    @Element(name = "title", required = true)
    String mTitle;
    @ElementList(name = "entry", required = false, inline = true)
    List<Entry> mEntryList;

    public Feed() {}

    public String getTitle() {
        return mTitle;
    }

    public List<Entry> getEntries() {
        return mEntryList;
    }
}
