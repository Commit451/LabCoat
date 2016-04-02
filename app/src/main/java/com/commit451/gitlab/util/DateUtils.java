package com.commit451.gitlab.util;

import android.content.Context;

import com.commit451.gitlab.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Our own DateUtils, which call forwards to {@link android.text.format.DateUtils} with some
 * nice defaults
 */
public class DateUtils {

    private static DateFormat FORMATTER = new SimpleDateFormat("HH:mm:ss");

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
        Date timeTaken = new Date(endTime.getTime() - startTime.getTime());
        return FORMATTER.format(timeTaken);
    }
}
