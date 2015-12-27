package com.commit451.gitlab.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.event.LoginEvent;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.model.api.UserLogin;
import com.commit451.gitlab.ssl.X509CertificateException;
import com.commit451.gitlab.ssl.X509Util;
import com.commit451.gitlab.util.KeyboardUtil;
import com.commit451.gitlab.util.NavigationManager;
import com.commit451.gitlab.view.EmailAutoCompleteTextView;

import java.security.cert.CertificateEncodingException;
import java.util.Date;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class LoginActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_GET_ACCOUNTS = 1337;
    private static Pattern mUrlPattern = Patterns.WEB_URL;

    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        return intent;
    }

    @Bind(R.id.root) View mRoot;
    @Bind(R.id.url_hint) TextInputLayout mUrlHint;
    @Bind(R.id.url_input) TextView mUrlInput;
    @Bind(R.id.user_input_hint) TextInputLayout mUserHint;
    @Bind(R.id.user_input) EmailAutoCompleteTextView mUserInput;
    @Bind(R.id.password_hint) TextInputLayout mPasswordHint;
    @Bind(R.id.password_input) TextView mPasswordInput;
    @Bind(R.id.token_hint) TextInputLayout mTokenHint;
    @Bind(R.id.token_input) TextView mTokenInput;
    @Bind(R.id.normal_login) View mNormalLogin;
    @Bind(R.id.token_login) View mTokenLogin;
    @Bind(R.id.progress) View mProgress;

    private boolean mIsNormalLogin = true;
    private String mTrustedCertificate;
    private Account mAccount;

    private final TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            onLoginClick();
            return true;
        }
    };

    @OnClick(R.id.show_normal_link)
    public void showNormalLogin(TextView loginTypeTextView) {
        if (mNormalLogin.getVisibility() == View.VISIBLE) {
            mNormalLogin.setVisibility(View.GONE);
            mTokenLogin.setVisibility(View.VISIBLE);
            loginTypeTextView.setText(R.string.normal_link);
            mIsNormalLogin = false;
        } else {
            mNormalLogin.setVisibility(View.VISIBLE);
            mTokenLogin.setVisibility(View.GONE);
            loginTypeTextView.setText(R.string.token_link);
            mIsNormalLogin = true;
        }
    }

    @OnClick(R.id.login_button)
    public void onLoginClick() {
        KeyboardUtil.hideKeyboard(this);
        if (hasEmptyFields(mUrlHint)) {
            return;
        }
        if (mIsNormalLogin) {
            if (hasEmptyFields(mUrlHint, mUserHint, mPasswordHint)) {
                return;
            }
            if (!mUrlPattern.matcher(mUrlInput.getText()).matches()) {
                mUrlHint.setError(getString(R.string.not_a_valid_url));
                return;
            } else {
                mUrlHint.setError(null);
            }
        }
        if (!mIsNormalLogin && hasEmptyFields(mTokenHint)) {
            return;
        }
        String url = mUrlInput.getText().toString();
        mAccount = new Account();
        mAccount.setServerUrl(Uri.parse(url));
        mAccount.setTrustedCertificate(mTrustedCertificate);

        GitLabClient.setAccount(mAccount);

        if(mIsNormalLogin) {
            connect(true);
        }
        else {
            connect(false);
        }
    }

    private Callback<UserLogin> mLoginCallback = new Callback<UserLogin>() {

        @Override
        public void onResponse(Response<UserLogin> response, Retrofit retrofit) {
            mTrustedCertificate = null;
            if (!response.isSuccess()) {
                handleConnectionResponse(response.code());
                return;
            }
            mAccount.setPrivateToken(response.body().getPrivateToken());
            GitLabClient.setAccount(mAccount);
            loadUser();
        }

        @Override
        public void onFailure(Throwable t) {
            mTrustedCertificate = null;
            Timber.e(t, null);
            handleConnectionError(t);
        }
    };

    private Callback<UserFull> mTestUserCallback = new Callback<UserFull>() {
        @Override
        public void onResponse(Response<UserFull> response, Retrofit retrofit) {
            mProgress.setVisibility(View.GONE);
            if (!response.isSuccess()) {
                handleConnectionResponse(response.code());
                return;
            }
            mAccount.setUser(response.body());
            mAccount.setLastUsed(new Date());
            Prefs.addAccount(LoginActivity.this, mAccount);
            GitLabClient.setAccount(mAccount);
            GitLabApp.bus().post(new LoginEvent(mAccount));
            NavigationManager.navigateToProjects(LoginActivity.this);
            finish();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            Snackbar.make(mRoot, getString(R.string.login_error), Snackbar.LENGTH_LONG)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mPasswordInput.setOnEditorActionListener(onEditorActionListener);
        mTokenInput.setOnEditorActionListener(onEditorActionListener);
        mUserInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    checkAccountPermission();
                }
            }
        });
    }

    @TargetApi(23)
    private void checkAccountPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
            mUserInput.retrieveAccounts();
        } else {
            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, PERMISSION_REQUEST_GET_ACCOUNTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_GET_ACCOUNTS: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mUserInput.retrieveAccounts();
                }
            }
        }
    }

    private void connect(boolean byAuth) {
        mProgress.setVisibility(View.VISIBLE);
        mProgress.setAlpha(0.0f);
        mProgress.animate().alpha(1.0f);

        if(byAuth) {
            connectByAuth();
        }
        else {
            connectByToken();
        }
    }

    private void connectByAuth() {
        if(mUserInput.getText().toString().contains("@")) {
            GitLabClient.instance().loginWithEmail(mUserInput.getText().toString(), mPasswordInput.getText().toString()).enqueue(mLoginCallback);
        }
        else {
            GitLabClient.instance().loginWithUsername(mUserInput.getText().toString(), mPasswordInput.getText().toString()).enqueue(mLoginCallback);
        }
    }

    private void connectByToken() {
        mAccount.setPrivateToken(mTokenInput.getText().toString());
        GitLabClient.setAccount(mAccount);
        loadUser();
    }

    private void loadUser() {
        GitLabClient.instance().getThisUser().enqueue(mTestUserCallback);
    }

    private void handleConnectionError(Throwable t) {
        mProgress.setVisibility(View.GONE);

        if(t instanceof SSLHandshakeException && t.getCause() instanceof X509CertificateException) {
            String fingerprint = null;
            try {
                fingerprint = X509Util.getFingerPrint(((X509CertificateException) t.getCause()).getChain()[0]);
            } catch (CertificateEncodingException e) {
                Timber.e(e, null);
            }
            final String finalFingerprint = fingerprint;

            Dialog d = new AlertDialog.Builder(this)
                    .setTitle(R.string.certificate_title)
                    .setMessage(String.format(getResources().getString(R.string.certificate_message), finalFingerprint))
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (finalFingerprint != null) {
                                mTrustedCertificate = finalFingerprint;
                                onLoginClick();
                            }

                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

            ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            Snackbar.make(mRoot, getString(R.string.login_error), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void handleConnectionResponse(int responseCode) {
        mProgress.setVisibility(View.GONE);
        switch (responseCode) {
            case 401:
                Snackbar.make(mRoot, getString(R.string.login_unauthorized), Snackbar.LENGTH_LONG)
                        .show();
                return;
            default:
                Snackbar.make(mRoot, getString(R.string.login_error), Snackbar.LENGTH_LONG)
                        .show();
        }
    }
}
