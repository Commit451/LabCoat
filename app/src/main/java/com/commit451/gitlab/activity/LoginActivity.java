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
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.commit451.gitlab.LabCoatApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.dialog.HttpLoginDialog;
import com.commit451.gitlab.event.LoginEvent;
import com.commit451.gitlab.event.ReloadDataEvent;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.model.api.UserLogin;
import com.commit451.gitlab.ssl.X509CertificateException;
import com.commit451.gitlab.ssl.X509Util;
import com.commit451.gitlab.util.KeyboardUtil;
import com.commit451.gitlab.util.NavigationManager;
import com.commit451.gitlab.view.EmailAutoCompleteTextView;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.HttpUrl;

import java.security.cert.CertificateEncodingException;
import java.util.Date;
import java.util.List;
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
    private static Pattern sTokenPattern = Pattern.compile("^[A-Za-z0-9-_]*$");

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
    private String mAuthorizationHeader;
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

        Uri uri = null;
        try {
            String url = mUrlInput.getText().toString();
            if (HttpUrl.parse(url) != null) {
                uri = Uri.parse(url);
            }
        } catch (Exception e) {
            Timber.e(e, null);
        }

        if (uri == null) {
            mUrlHint.setError(getString(R.string.not_a_valid_url));
            return;
        } else {
            mUrlHint.setError(null);
        }

        if (mIsNormalLogin) {
            if (hasEmptyFields(mUrlHint, mUserHint, mPasswordHint)) {
                return;
            }
        } else {
            if (hasEmptyFields(mTokenHint)) {
                return;
            }
            if (!sTokenPattern.matcher(mTokenInput.getText()).matches()) {
                mTokenHint.setError(getString(R.string.not_a_valid_private_token));
                return;
            } else {
                mTokenHint.setError(null);
            }
        }

        if (isAlreadySignedIn(uri.toString(), mIsNormalLogin ? mUserInput.getText().toString() : mTokenInput.getText().toString())) {
            Snackbar.make(mRoot, getString(R.string.already_logged_in), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        mAccount = new Account();
        mAccount.setServerUrl(uri);
        mAccount.setTrustedCertificate(mTrustedCertificate);
        mAccount.setAuthorizationHeader(mAuthorizationHeader);

        if (mIsNormalLogin) {
            connect(true);
        } else {
            connect(false);
        }
    }

    private Callback<UserLogin> mLoginCallback = new Callback<UserLogin>() {

        @Override
        public void onResponse(Response<UserLogin> response, Retrofit retrofit) {
            mTrustedCertificate = null;
            if (!response.isSuccess()) {
                handleConnectionResponse(response);
                return;
            }
            mAccount.setPrivateToken(response.body().getPrivateToken());
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
                handleConnectionResponse(response);
                return;
            }
            mAccount.setUser(response.body());
            mAccount.setLastUsed(new Date());
            Prefs.addAccount(LoginActivity.this, mAccount);
            GitLabClient.setAccount(mAccount);
            LabCoatApp.bus().post(new LoginEvent(mAccount));
            //This is mostly for if projects already exists, then we will reload the data
            LabCoatApp.bus().post(new ReloadDataEvent());
            NavigationManager.navigateToProjects(LoginActivity.this);
            finish();
        }

        @Override
        public void onFailure(Throwable t) {
            mTrustedCertificate = null;
            Timber.e(t, null);
            handleConnectionError(t);
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
            GitLabClient.instance(mAccount).loginWithEmail(mUserInput.getText().toString(), mPasswordInput.getText().toString()).enqueue(mLoginCallback);
        }
        else {
            GitLabClient.instance(mAccount).loginWithUsername(mUserInput.getText().toString(), mPasswordInput.getText().toString()).enqueue(mLoginCallback);
        }
    }

    private void connectByToken() {
        mAccount.setPrivateToken(mTokenInput.getText().toString());
        loadUser();
    }

    private void loadUser() {
        GitLabClient.instance(mAccount).getThisUser().enqueue(mTestUserCallback);
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

    private void handleConnectionResponse(Response response) {
        mProgress.setVisibility(View.GONE);
        switch (response.code()) {
            case 401:
                String header = response.headers().get("WWW-Authenticate");
                if (header != null) {
                    handleBasicAuthentication(response);
                    return;
                }
                Snackbar.make(mRoot, getString(R.string.login_unauthorized), Snackbar.LENGTH_LONG)
                        .show();
                return;
            default:
                Snackbar.make(mRoot, getString(R.string.login_error), Snackbar.LENGTH_LONG)
                        .show();
        }
    }

    private void handleBasicAuthentication(Response response) {
        String header = response.headers().get("WWW-Authenticate").trim();
        if (!header.startsWith("Basic")) {
            Snackbar.make(mRoot, getString(R.string.login_unsupported_authentication), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        int realmStart = header.indexOf('"') + 1;
        int realmEnd = header.lastIndexOf('"');
        String realm = "";
        if (realmStart > 0 && realmEnd > -1) {
            realm = header.substring(realmStart, realmEnd);
        }

        HttpLoginDialog dialog = new HttpLoginDialog(this, realm, new HttpLoginDialog.LoginListener() {
            @Override
            public void onLogin(String username, String password) {
                mAuthorizationHeader = Credentials.basic(username, password);
                onLoginClick();
            }

            @Override
            public void onCancel() {
                mAuthorizationHeader = null;
            }
        });
        dialog.show();
    }

    private boolean isAlreadySignedIn(@NonNull String url, @NonNull String usernameOrEmailOrPrivateToken) {
        List<Account> accounts = Prefs.getAccounts(this);
        for (Account account : accounts) {
            if (account.getServerUrl().equals(Uri.parse(url))) {
                if (usernameOrEmailOrPrivateToken.equals(account.getUser().getUsername())
                        || usernameOrEmailOrPrivateToken.equalsIgnoreCase(account.getUser().getEmail())
                        || usernameOrEmailOrPrivateToken.equalsIgnoreCase(account.getPrivateToken())) {
                    return true;
                }
            }
        }
        return false;
    }
}
