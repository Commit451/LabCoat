package com.commit451.gitlab.util


import android.net.Uri
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.MotionEvent
import android.widget.TextView
import com.commit451.gitlab.navigation.Navigator
import timber.log.Timber

/**
 * Set this on a textview and then you can potentially open links locally if applicable
 */
class InternalLinkMovementMethod(private val serverUrl: Uri) : LinkMovementMethod() {

    override fun onTouchEvent(widget: TextView, buffer: android.text.Spannable, event: android.view.MotionEvent): Boolean {
        val action = event.action

        //http://stackoverflow.com/questions/1697084/handle-textview-link-click-in-my-android-app
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

            val link = buffer.getSpans(off, off, URLSpan::class.java)
            if (link.isNotEmpty()) {
                val url = link[0].url
                if (url.startsWith(serverUrl.toString())) {
                    Timber.d("Looks like an internal server link: %s", url)
                    Navigator.navigateToUrl(widget.context, Uri.parse(url))
                    return true
                }
                return super.onTouchEvent(widget, buffer, event)
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}
