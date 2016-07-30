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
    EditText mTextNote;

    @OnClick(R.id.button_send)
    void onSend() {
        if (mCallbacks != null) {
            mCallbacks.onSendClicked(mTextNote.getText().toString());
        }
    }

    @OnClick(R.id.icon_gallery)
    void onGalleryClicked() {
        if (mCallbacks != null) {
            mCallbacks.onGalleryClicked();
        }
    }

    @OnClick(R.id.icon_camera)
    void onCameraClicked() {
        if (mCallbacks != null) {
            mCallbacks.onCameraClicked();
        }
    }

    private Callbacks mCallbacks;

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
        setOrientation(VERTICAL);
        ButterKnife.bind(this);
        setBackgroundColor(Easel.getThemeAttrColor(getContext(), R.attr.colorPrimary));
        if (Build.VERSION.SDK_INT >= 21) {
            setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));
        }

        mTextNote.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onSend();
                return true;
            }
        });
    }

    public void setCallbacks(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    public void clearText() {
        mTextNote.setText("");
    }

    public void appendText(CharSequence text) {
        mTextNote.append(text);
    }

    public interface Callbacks {
        void onSendClicked(String message);
        void onGalleryClicked();
        void onCameraClicked();
    }
}
