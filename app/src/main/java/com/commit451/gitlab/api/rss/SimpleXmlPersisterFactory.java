package com.commit451.gitlab.api.rss;

import android.net.Uri;

import com.commit451.gitlab.model.Account;

import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.util.Date;

public final class SimpleXmlPersisterFactory {

    public static Persister createPersister(final Account account) {
        return new Persister(new Matcher() {
            @Override
            public Transform match(Class type) throws Exception {
                if (Date.class.isAssignableFrom(type)) {
                    return new DateTransform();
                } else if (Uri.class.isAssignableFrom(type)) {
                    return new UriTransform(account);
                }

                return null;
            }
        });
    }
}
