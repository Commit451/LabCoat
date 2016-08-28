package com.commit451.gitlab.util;


import android.net.Uri;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import com.commit451.gitlab.navigation.Navigator;

import timber.log.Timber;

/**
 * Set this on a textview and then you can potentially open links locally if applicable
 */
public class InternalLinkMovementMethod extends LinkMovementMethod {

    private Uri mServerUrl;

    public InternalLinkMovementMethod(Uri serverUrl) {
        mServerUrl = serverUrl;
    }

    public boolean onTouchEvent(TextView widget, android.text.Spannable buffer, android.view.MotionEvent event) {
        int action = event.getAction();

        //http://stackoverflow.com/questions/1697084/handle-textview-link-click-in-my-android-app
        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
            if (link.length != 0) {
                String url = link[0].getURL();
                if (url.startsWith(mServerUrl.toString())) {
                    Timber.d("Looks like an internal server link: %s", url);
                    Navigator.navigateToUrl(widget.getContext(), Uri.parse(url));
                    return true;
                }
                return super.onTouchEvent(widget, buffer, event);
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }
}
