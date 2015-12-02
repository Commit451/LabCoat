package com.commit451.gitlab.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.Session;
import com.commit451.gitlab.model.User;
import com.commit451.gitlab.ssl.CustomTrustManager;
import com.commit451.gitlab.ssl.X509CertificateException;
import com.commit451.gitlab.ssl.X509Util;
import com.commit451.gitlab.tools.KeyboardUtil;
import com.commit451.gitlab.tools.NavigationManager;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.views.EmailAutoCompleteTextView;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
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
    private Pattern mUrlPattern = Patterns.WEB_URL;

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
        GitLabClient.reset();

        String url = mUrlInput.getText().toString();

        if(url.startsWith("http://") && url.endsWith(".git")) {
            mUrlInput.setText(url.substring(0, nthOccurrence(url, '/', 2)));
        }
        else if(url.startsWith("git@") && url.endsWith(".git")) {
            mUrlInput.setText("http://" + url.substring(4, url.indexOf(':')));
        }
        else if(!url.startsWith("http://") && !url.startsWith("https://")) {
            mUrlInput.setText("http://" + mUrlInput.getText().toString());
        }

        if(mIsNormalLogin) {
            connect(true);
        }
        else {
            connect(false);
        }
    }

    private Callback<Session> mSessionCallback = new Callback<Session>() {

        @Override
        public void onResponse(Response<Session> response, Retrofit retrofit) {
            mProgress.setVisibility(View.GONE);
            if (!response.isSuccess()) {
                handleConnectionResponse(response.code());
                return;
            }

            Prefs.setLoggedIn(LoginActivity.this, true);
            Prefs.setPrivateToken(LoginActivity.this, response.body().getPrivateToken());

            Intent i = new Intent(LoginActivity.this, GitlabActivity.class);
            startActivity(i);
            finish();
        }

        @Override
        public void onFailure(Throwable t) {
            handleConnectionError(t);
        }
    };

    private Callback<User> mTestUserCallback = new Callback<User>() {
        @Override
        public void onResponse(Response<User> response, Retrofit retrofit) {
            mProgress.setVisibility(View.GONE);
            if (!response.isSuccess()) {
                handleConnectionResponse(response.code());
                return;
            }
            Prefs.setLoggedIn(LoginActivity.this, true);
            NavigationManager.navigateToProjects(LoginActivity.this);
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t.toString());
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

        CustomTrustManager.setTrustedCertificates(Prefs.getTrustedCertificates(this));
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

    public static int nthOccurrence(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos + 1);
        return pos;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void connect(boolean byAuth) {
        mProgress.setVisibility(View.VISIBLE);
        mProgress.setAlpha(0.0f);
        mProgress.animate().alpha(1.0f);

        Prefs.setPrivateToken(this, "");
        Prefs.setLoggedIn(this, false);
        Prefs.setServerUrl(this, mUrlInput.getText().toString());

        if(byAuth) {
            connectByAuth();
        }
        else {
            connectByToken();
        }
    }

    private void connectByAuth() {
        if(mUserInput.getText().toString().contains("@")) {
            GitLabClient.instance().getSessionByEmail(mUserInput.getText().toString(), mPasswordInput.getText().toString()).enqueue(mSessionCallback);
        }
        else {
            GitLabClient.instance().getSessionByUsername(mUserInput.getText().toString(), mPasswordInput.getText().toString()).enqueue(mSessionCallback);
        }
    }

    private void connectByToken() {
        Prefs.setPrivateToken(this, mTokenInput.getText().toString());
        GitLabClient.instance().getUser().enqueue(mTestUserCallback);
    }

    private void handleConnectionError(Throwable e) {
        Timber.e(e.toString());

        mProgress.setVisibility(View.GONE);

        if(e instanceof SSLHandshakeException && e.getCause() instanceof X509CertificateException) {
            String fingerprint = null;
            try {
                fingerprint = X509Util.getFingerPrint(((X509CertificateException) e.getCause()).getChain()[0]);
            } catch (CertificateEncodingException ex) {
                Timber.e(e.toString());
            }
            final String finalFingerprint = fingerprint;

            Dialog d = new AlertDialog.Builder(this)
                    .setTitle(R.string.certificate_title)
                    .setMessage(String.format(getResources().getString(R.string.certificate_message), finalFingerprint))
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (finalFingerprint != null) {
                                Set<String> trustedCertificates = new HashSet<>(Prefs.getTrustedCertificates(LoginActivity.this));
                                trustedCertificates.add(finalFingerprint);
                                Prefs.setTrustedCertificates(LoginActivity.this, trustedCertificates);
                                CustomTrustManager.setTrustedCertificates(trustedCertificates);
                            }

                            dialog.dismiss();

                            if (finalFingerprint != null) {
                                onLoginClick();
                            }
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
