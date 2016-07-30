package com.commit451.gitlab.api.rss;

import com.commit451.gitlab.util.ConversionUtil;

import org.simpleframework.xml.transform.Transform;

import java.util.Date;

/**
 * Transforms dates!
 */
public class DateTransform implements Transform<Date> {

    public DateTransform() {
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
