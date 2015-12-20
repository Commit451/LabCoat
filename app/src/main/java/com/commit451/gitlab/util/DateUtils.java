package com.commit451.gitlab.util;

import android.content.Context;

import com.commit451.gitlab.R;

import java.util.Date;

/**
 * Our own DateUtils, which call forwards to {@link android.text.format.DateUtils} with some
 * nice defaults
 * Created by Jawnnypoo on 11/17/2015.
 */
public class DateUtils {

    public static CharSequence getRelativeTimeSpanString(Context context, Date startTime) {
        Date now = new Date();
        if (now.getTime() - startTime.getTime() < android.text.format.DateUtils.SECOND_IN_MILLIS) {
            return context.getString(R.string.just_now);
        }
        return android.text.format.DateUtils.getRelativeTimeSpanString(startTime.getTime(),
                now.getTime(),
                android.text.format.DateUtils.SECOND_IN_MILLIS);
    }
}
