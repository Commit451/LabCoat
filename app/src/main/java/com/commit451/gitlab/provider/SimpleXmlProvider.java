package com.commit451.gitlab.provider;

import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.util.ConversionUtil;

import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import android.net.Uri;

import java.util.Date;

public final class SimpleXmlProvider {
    private static Persister sPersister;

    private SimpleXmlProvider() {}

    public static Persister getPersister() {
        if (sPersister == null) {
            sPersister = createPersister(null);
        }
        return sPersister;
    }

    public static Persister createPersister(final Account account) {
        return new Persister(new Matcher() {
            @Override
            public Transform match(Class type) throws Exception {
                if (Date.class.isAssignableFrom(type)) {
                    return new DateTransform(account);
                } else if (Uri.class.isAssignableFrom(type)) {
                    return new UriTransform(account);
                }

                return null;
            }
        });
    }

    private static class DateTransform implements Transform<Date> {
        private final Account mAccount;

        public DateTransform(Account account) {
            mAccount = account;
        }

        @Override
        public Date read(String value) throws Exception {
            return ConversionUtil.toDate(value);
        }

        @Override
        public String write(Date value) throws Exception {
            return ConversionUtil.fromDate(value);
        }
    }

    private static class UriTransform implements Transform<Uri> {
        private final Account mAccount;

        public UriTransform(Account account) {
            mAccount = account;
        }

        @Override
        public Uri read(String value) throws Exception {
            return ConversionUtil.toUri(mAccount, value);
        }

        @Override
        public String write(Uri value) throws Exception {
            return ConversionUtil.fromUri(value);
        }
    }
}
