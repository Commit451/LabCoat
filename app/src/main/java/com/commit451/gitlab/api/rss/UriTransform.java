package com.commit451.gitlab.api.rss;

import android.net.Uri;

import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.util.ConversionUtil;

import org.simpleframework.xml.transform.Transform;

/**
 * Uri Transformer
 */
public class UriTransform implements Transform<Uri> {
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
