package com.commit451.gitlab.util;

import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.model.api.Project;
import com.squareup.picasso.Picasso;
import com.vdurmont.emoji.EmojiParser;

import in.uncod.android.bypass.Bypass;

public class MarkdownUtil {

    public static CharSequence from(TextView textView, Bypass bypass, Picasso picasso, String serverUrl, Project project, String markdown) {
        markdown = EmojiParser.parseToUnicode(markdown);
        return bypass.markdownToSpannable(markdown,
                BypassImageGetterFactory.create(textView,
                        App.Companion.get().getPicasso(),
                        App.Companion.get().getAccount().getServerUrl().toString(),
                        project));
    }
}
