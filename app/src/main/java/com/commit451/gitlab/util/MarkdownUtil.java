package com.commit451.gitlab.util;

import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.model.api.Project;
import com.squareup.picasso.Picasso;
import com.vdurmont.emoji.EmojiParser;

import in.uncod.android.bypass.Bypass;

/**
 * Created by Jawn on 8/25/2016.
 */

public class MarkdownUtil {

    public static CharSequence from(TextView textView, Bypass bypass, Picasso picasso, String serverUrl, Project project, String markdown) {
        markdown = EmojiParser.parseToUnicode(markdown);
        CharSequence charSequence = bypass.markdownToSpannable(markdown,
                BypassImageGetterFactory.create(textView,
                        App.instance().getPicasso(),
                        App.instance().getAccount().getServerUrl().toString(),
                        project));
        return charSequence;
    }
}
