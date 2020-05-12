package com.commit451.gitlab.util


import android.net.Uri
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView
import com.commit451.gitlab.navigation.Navigator
import timber.log.Timber

/**
 * Set this on a textview and then you can potentially open links locally if applicable
 */
class InternalLinkMovementMethod(private val serverUrl: String) : LinkMovementMethod() {

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.action

        //http://stackoverflow.com/a/16644228/895797
        if (action == MotionEvent.ACTION_UP) {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val links = buffer.getSpans(off, off, URLSpan::class.java)
            if (links.isNotEmpty()) {
                val url = links[0].url
                if (url.startsWith(serverUrl)) {
                    Timber.d("Looks like an internal server link: %s", url)
                    Navigator.navigateToUrl(widget.context, Uri.parse(url))
                    return true
                }
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}
