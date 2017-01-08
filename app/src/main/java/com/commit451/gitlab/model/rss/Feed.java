package com.commit451.gitlab.model.rss;

import org.parceler.Parcel;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Parcel
@Root(strict = false)
public class Feed {
    @Element(name = "title", required = false)
    String title;
    @ElementList(name = "entry", required = false, inline = true)
    List<Entry> entryList;

    public Feed() {}

    public String getTitle() {
        return title;
    }

    public List<Entry> getEntries() {
        return entryList;
    }
}
