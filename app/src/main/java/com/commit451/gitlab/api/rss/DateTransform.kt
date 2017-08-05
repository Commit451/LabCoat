package com.commit451.gitlab.api.rss

import org.simpleframework.xml.transform.Transform
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * Transforms dates!
 */
class DateTransform : Transform<Date> {

    override fun read(value: String): Date? {
        return Date(Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value)).toEpochMilli())
    }

    override fun write(value: Date): String {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(Instant.ofEpochMilli(value.time))
    }
}
