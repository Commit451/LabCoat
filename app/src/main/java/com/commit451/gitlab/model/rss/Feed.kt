package com.commit451.gitlab.model.rss

import org.parceler.Parcel
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Parcel(Parcel.Serialization.BEAN)
@Root(strict = false)
class Feed {
    @field:Element(name = "title", required = false)
    lateinit var title: String
    @field:ElementList(name = "entry", required = false, inline = true)
    var entries: List<Entry>? = null
}
