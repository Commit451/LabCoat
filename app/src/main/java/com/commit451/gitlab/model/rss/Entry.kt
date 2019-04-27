package com.commit451.gitlab.model.rss

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.util.*

@Root(strict = false)
class Entry {
    @field:Element(name = "link", required = true)
    lateinit var link: Link
    @field:Element(name = "title", required = true)
    lateinit var title: String
    @field:Element(name = "updated", required = true)
    lateinit var updated: Date
    @field:Element(name = "thumbnail", required = true)
    lateinit var thumbnail: Thumbnail
    @field:Element(name = "summary", required = true)
    lateinit var summary: String
}
