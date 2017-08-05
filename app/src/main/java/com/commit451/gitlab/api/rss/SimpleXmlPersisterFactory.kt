package com.commit451.gitlab.api.rss

import org.simpleframework.xml.core.Persister
import org.simpleframework.xml.transform.Matcher
import java.util.*

object SimpleXmlPersisterFactory {

    fun createPersister(): Persister {
        return Persister(Matcher { type ->
            if (Date::class.java.isAssignableFrom(type)) {
                return@Matcher DateTransform()
            }

            null
        })
    }
}
