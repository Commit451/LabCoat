package com.commit451.gitlab.util;

import android.content.Context;

import com.commit451.gitlab.R;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Our own DateUtils, which call forwards to {@link android.text.format.DateUtils} with some
 * nice defaults
 */
public class DateUtils {

    public static CharSequence getRelativeTimeSpanString(Context context, Date startTime) {
        Date now = new Date();
        if (Math.abs(now.getTime() - startTime.getTime()) < android.text.format.DateUtils.SECOND_IN_MILLIS) {
            return context.getString(R.string.just_now);
        }

        return android.text.format.DateUtils.getRelativeTimeSpanString(startTime.getTime(),
                now.getTime(),
                android.text.format.DateUtils.SECOND_IN_MILLIS)
                .toString();
    }

    public static String getTimeTaken(Date startTime, Date endTime) {
        return android.text.format.DateUtils.formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(endTime.getTime() - startTime.getTime()));
    }
}
