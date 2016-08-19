package com.commit451.gitlab.util;

import android.widget.TextView;

import com.commit451.bypasspicassoimagegetter.BypassPicassoImageGetter;
import com.commit451.gitlab.model.api.Project;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

/**
 * Creates {@link BypassPicassoImageGetter}s which are configured to handle relative Urls
 */
public class BypassImageGetterFactory {

    public static BypassPicassoImageGetter create(TextView textView, Picasso picasso, final String baseUrl, final Project project) {
        BypassPicassoImageGetter getter = new BypassPicassoImageGetter(textView, picasso);
        getter.setSourceModifier(new BypassPicassoImageGetter.SourceModifier() {
            @Override
            public String modify(String source) {
                if (source.startsWith("/")) {
                    String url = baseUrl + "/" + project.getPathWithNamespace() + source;
                    Timber.d(url);
                    return url;
                }
                return source;
            }
        });
        return getter;
    }
}
