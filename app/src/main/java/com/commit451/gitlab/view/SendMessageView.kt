package com.commit451.gitlab.view

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.commit451.addendum.themeAttrColor
import com.commit451.gitlab.R

/**
 * View that show UI for sending a message
 */
class SendMessageView : LinearLayout {

    @BindView(R.id.text_note)
    lateinit var textNote: EditText

    @OnClick(R.id.button_send)
    fun onSend() {
        if (callback != null) {
            callback!!.onSendClicked(textNote.text.toString())
        }
    }

    @OnClick(R.id.button_attach)
    fun onAttachClicked() {
        if (callback != null) {
            callback!!.onAttachmentClicked()
        }
    }

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

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.view_send_message, this)
        orientation = LinearLayout.HORIZONTAL
        ButterKnife.bind(this)
        setBackgroundColor(context.themeAttrColor(R.attr.colorPrimary))
        if (Build.VERSION.SDK_INT >= 21) {
            elevation = resources.getDimensionPixelSize(R.dimen.toolbar_elevation).toFloat()
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
