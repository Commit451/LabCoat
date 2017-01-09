package com.commit451.gitlab.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * View that show UI for sending a message
 */
public class SendMessageView extends LinearLayout {

    @BindView(R.id.text_note)
    EditText textNote;

    @OnClick(R.id.button_send)
    void onSend() {
        if (callback != null) {
            callback.onSendClicked(textNote.getText().toString());
        }
    }

    @OnClick(R.id.button_attach)
    void onAttachClicked() {
        if (callback != null) {
            callback.onAttachmentClicked();
        }
    }

    private Callback callback;

    public SendMessageView(Context context) {
        super(context);
        init();
    }

    public SendMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SendMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public SendMessageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_send_message, this);
        setOrientation(HORIZONTAL);
        ButterKnife.bind(this);
        setBackgroundColor(Easel.getThemeAttrColor(getContext(), R.attr.colorPrimary));
        if (Build.VERSION.SDK_INT >= 21) {
            setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));
        }

        textNote.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onSend();
                return true;
            }
        });
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void clearText() {
        textNote.setText("");
    }

    public void appendText(CharSequence text) {
        textNote.append(text);
    }

    public interface Callback {
        void onSendClicked(String message);
        void onAttachmentClicked();
    }
}
