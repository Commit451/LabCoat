package com.commit451.gitlab.model.rss

import org.parceler.Parcel
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Root

@Parcel(Parcel.Serialization.BEAN)
@Root(strict = false)
class Link {
    @Attribute(name = "href", required = true)
    lateinit var href: String
}
