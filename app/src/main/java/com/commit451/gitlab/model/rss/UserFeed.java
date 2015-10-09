package com.commit451.gitlab.model.rss;

import org.parceler.Parcel;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Feed for a user, pulled from RSS
 * Created by John on 10/8/15.
 */
@Root(strict = false)
@Parcel
public class UserFeed {

    @Element(name = "title")
    String mTitle;
    @ElementList(name = "entry", required = false, inline = true)
    List<Entry> mEntryList;

    public UserFeed() {

    }

    public String getTitle() {
        return mTitle;
    }

    public List<Entry> getEntries() {
        return mEntryList;
    }
}
