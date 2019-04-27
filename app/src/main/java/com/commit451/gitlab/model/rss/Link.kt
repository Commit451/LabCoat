package com.commit451.gitlab.model.rss

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Root

@Root(strict = false)
class Link {
    @field:Attribute(name = "href", required = true)
    lateinit var href: String
}
