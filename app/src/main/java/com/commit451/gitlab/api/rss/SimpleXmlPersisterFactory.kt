package com.commit451.gitlab.api.rss

import android.net.Uri
import com.commit451.gitlab.model.Account
import org.simpleframework.xml.core.Persister
import org.simpleframework.xml.transform.Matcher
import java.util.*

object SimpleXmlPersisterFactory {

    fun createPersister(account: Account): Persister {
        return Persister(Matcher { type ->
            if (Date::class.java.isAssignableFrom(type)) {
                return@Matcher DateTransform()
            } else if (Uri::class.java.isAssignableFrom(type)) {
                return@Matcher UriTransform(account)
            }

            null
        })
    }
}
