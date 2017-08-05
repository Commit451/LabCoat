package com.commit451.gitlab.model.rss

import org.parceler.Parcel
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.util.*

@Parcel(Parcel.Serialization.BEAN)
@Root(strict = false)
class Entry {
    @Element(name = "link", required = true)
    lateinit var link: Link
    @Element(name = "title", required = true)
    lateinit var title: String
    @Element(name = "updated", required = true)
    lateinit var updated: Date
    @Element(name = "thumbnail", required = true)
    lateinit var thumbnail: Thumbnail
    @Element(name = "summary", required = true)
    lateinit var summary: String
}
