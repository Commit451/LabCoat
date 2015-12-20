package com.commit451.gitlab.provider;

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
            sPersister = createPersister();
        }
        return sPersister;
    }

    private static Persister createPersister() {
        return new Persister(new Matcher() {
            @Override
            public Transform match(Class type) throws Exception {
                if (Date.class.isAssignableFrom(type)) {
                    return new DateTransform();
                } else if (Uri.class.isAssignableFrom(type)) {
                    return new UriTransform();
                }

                return null;
            }
        });
    }

    private static class DateTransform implements Transform<Date> {
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
        @Override
        public Uri read(String value) throws Exception {
            return ConversionUtil.toUri(value);
        }

        @Override
        public String write(Uri value) throws Exception {
            return ConversionUtil.fromUri(value);
        }
    }
}
