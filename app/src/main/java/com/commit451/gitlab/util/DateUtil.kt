package com.commit451.gitlab.util

import android.content.Context
import android.text.format.DateUtils
import com.commit451.gitlab.R
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Our own DateUtil, which call forwards to [android.text.format.DateUtils] with some
 * nice defaults
 */
object DateUtil {

    fun getRelativeTimeSpanString(context: Context, startTime: Date?): CharSequence {
        val now = Date()
        if (startTime == null) {
            return context.getString(R.string.unknown)
        }
        if (abs(now.time - startTime.time) < DateUtils.SECOND_IN_MILLIS) {
            return context.getString(R.string.just_now)
        }

        return DateUtils.getRelativeTimeSpanString(startTime.time,
                now.time,
                DateUtils.SECOND_IN_MILLIS)
                .toString()
    }

    fun getTimeTaken(startTime: Date, endTime: Date): String {
        return DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(endTime.time - startTime.time))
    }
}
