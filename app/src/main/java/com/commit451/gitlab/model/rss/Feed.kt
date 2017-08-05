package com.commit451.gitlab.model.rss

import org.parceler.Parcel
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Parcel(Parcel.Serialization.BEAN)
@Root(strict = false)
class Feed {
    @Element(name = "title", required = false)
    lateinit var title: String
    @ElementList(name = "entry", required = false, inline = true)
    lateinit var entries: List<Entry>
}
