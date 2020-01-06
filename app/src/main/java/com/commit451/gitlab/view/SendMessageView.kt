package com.commit451.gitlab.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.commit451.addendum.themeAttrColor
import com.commit451.gitlab.R
import kotlinx.android.synthetic.main.view_send_message.view.*

/**
 * View that show UI for sending a message
 */
class SendMessageView : LinearLayout {

    var callback: Callback? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.view_send_message, this)
        orientation = HORIZONTAL
        setBackgroundColor(context.themeAttrColor(R.attr.colorPrimary))
        elevation = resources.getDimensionPixelSize(R.dimen.toolbar_elevation).toFloat()
        buttonSend.setOnClickListener {
            callback?.onSendClicked(textNote.text.toString())
        }
        buttonAttach.setOnClickListener {
            callback?.onAttachmentClicked()
        }
    }

    fun clearText() {
        textNote.setText("")
    }

    fun appendText(text: CharSequence) {
        textNote.append(text)
    }

    interface Callback {
        fun onSendClicked(message: String)
        fun onAttachmentClicked()
    }
}
