package com.commit451.gitlab.api.rss

import com.commit451.gitlab.util.ConversionUtil
import org.simpleframework.xml.transform.Transform
import java.util.*

/**
 * Transforms dates!
 */
class DateTransform : Transform<Date> {

    @Throws(Exception::class)
    override fun read(value: String): Date? {
        return ConversionUtil.toDate(value)
    }

    @Throws(Exception::class)
    override fun write(value: Date): String {
        return ConversionUtil.fromDate(value)
    }
}
