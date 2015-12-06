package com.commit451.gitlab.tools;

import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Account;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import android.net.Uri;

import java.util.Date;

public final class ConversionUtil {
    private ConversionUtil() {}

    public static String fromDate(Date date) {
        return ISODateTimeFormat.dateTime().print(new DateTime(date));
    }

    public static Date toDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        return ISODateTimeFormat.dateTimeParser().parseDateTime(dateString).toDate();
    }

    public static String fromUri(Uri uri) {
        if (uri == null) {
            return null;
        }

        return uri.toString();
    }

    public static Uri toUri(String uriString) {
        if (uriString == null) {
            return null;
        }
        if (uriString.isEmpty()) {
            return Uri.EMPTY;
        }

        Uri uri = Uri.parse(uriString);
        if (!uri.isRelative()) {
            return uri;
        }

        Account account = GitLabClient.getAccount();
        if (account == null) {
            return uri;
        }

        Uri.Builder builder = Uri.parse(account.getServerUrl())
                .buildUpon()
                .encodedQuery(uri.getEncodedQuery())
                .encodedFragment(uri.getEncodedFragment());

        if (uri.getPath().startsWith("/")) {
            builder.encodedPath(uri.getEncodedPath());
        } else {
            builder.appendEncodedPath(uri.getEncodedPath());
        }

        return builder.build();
    }
}
