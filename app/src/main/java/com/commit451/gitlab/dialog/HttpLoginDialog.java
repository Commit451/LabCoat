package com.commit451.gitlab.dialog;

import com.commit451.gitlab.R;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HttpLoginDialog extends AppCompatDialog {

    @Bind(R.id.message_text) TextView mMessageTextView;
    @Bind(R.id.login_username) EditText mUsernameView;
    @Bind(R.id.login_password) EditText mPasswordView;
    @Bind(R.id.ok_button) Button mOkButton;
    @Bind(R.id.cancel_button) Button mCancelButton;

    public HttpLoginDialog(Context context, String realm, final LoginListener loginListener) {
        super(context);
        setContentView(R.layout.dialog_http_login);
        ButterKnife.bind(this);

        mMessageTextView.setText(String.format(context.getResources().getString(R.string.realm_message), realm));
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginListener.onLogin(mUsernameView.getText().toString(), mPasswordView.getText().toString());
                HttpLoginDialog.this.dismiss();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginListener.onCancel();
                HttpLoginDialog.this.dismiss();
            }
        });
        setTitle(R.string.login_activity);
    }

    public interface LoginListener {
        void onLogin(String username, String password);
        void onCancel();
    }
}
