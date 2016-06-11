package com.commit451.gitlab.activity;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.commit451.gitlab.App;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.dialog.HttpLoginDialog;
import com.commit451.gitlab.event.LoginEvent;
import com.commit451.gitlab.event.ReloadDataEvent;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.model.api.UserLogin;
import com.commit451.gitlab.ssl.CustomHostnameVerifier;
import com.commit451.gitlab.ssl.CustomKeyManager;
import com.commit451.gitlab.ssl.X509CertificateException;
import com.commit451.gitlab.ssl.X509Util;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.teleprinter.Teleprinter;

import java.net.ConnectException;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class LoginActivity extends BaseActivity {

    private static final String EXTRA_SHOW_CLOSE = "show_close";

    private static final int PERMISSION_REQUEST_GET_ACCOUNTS = 1337;
    private static Pattern sTokenPattern = Pattern.compile("^[A-Za-z0-9-_]*$");

    public static Intent newIntent(Context context) {
        return newIntent(context, false);
    }

    public static Intent newIntent(Context context, boolean showClose) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(EXTRA_SHOW_CLOSE, showClose);
        return intent;
    }

    @BindView(R.id.root)
    View mRoot;
    @BindView(R.id.close)
    View mClose;
    @BindView(R.id.url_hint)
    TextInputLayout mUrlHint;
    @BindView(R.id.url_input)
    TextView mUrlInput;
    @BindView(R.id.user_input_hint)
    TextInputLayout mUserHint;
    @BindView(R.id.user_input)
    AppCompatAutoCompleteTextView mUserInput;
    @BindView(R.id.password_hint)
    TextInputLayout mPasswordHint;
    @BindView(R.id.password_input)
    TextView mPasswordInput;
    @BindView(R.id.token_hint)
    TextInputLayout mTokenHint;
    @BindView(R.id.token_input)
    TextView mTokenInput;
    @BindView(R.id.normal_login)
    View mNormalLogin;
    @BindView(R.id.token_login)
    View mTokenLogin;
    @BindView(R.id.progress)
    View mProgress;

    private boolean mIsNormalLogin = true;
    private Account mAccount;
    private Teleprinter mTeleprinter;

    private final TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            onLoginClick();
            return true;
        }
    };

    @OnClick(R.id.close)
    public void onCloseClick() {
        onBackPressed();
    }

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
        mTeleprinter.hideKeyboard();

        if (hasEmptyFields(mUrlHint)) {
            return;
        }

        String url = mUrlInput.getText().toString();
        Uri uri = null;
        try {
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
        if (url.charAt(url.length()-1) != '/') {
            mUrlHint.setError(getString(R.string.please_end_your_url_with_a_slash));
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

        login();
    }

    private void login() {
        // This seems useless - But believe me, it makes everything work! Don't remove it.
        // (OkHttpClientProvider caches the clients and needs a new account to recreate them)

        Account newAccount = new Account();
        newAccount.setServerUrl(mAccount.getServerUrl());
        newAccount.setTrustedCertificate(mAccount.getTrustedCertificate());
        newAccount.setTrustedHostname(mAccount.getTrustedHostname());
        newAccount.setPrivateKeyAlias(mAccount.getPrivateKeyAlias());
        newAccount.setAuthorizationHeader(mAccount.getAuthorizationHeader());
        mAccount = newAccount;

        if (mIsNormalLogin) {
            connect(true);
        } else {
            connect(false);
        }
    }

    private void loginWithPrivateToken() {
        KeyChain.choosePrivateKeyAlias(this, new KeyChainAliasCallback() {
            @Override
            public void alias(String alias) {
                mAccount.setPrivateKeyAlias(alias);

                if (alias != null) {
                    if (!CustomKeyManager.isCached(alias)) {
                        CustomKeyManager.cache(LoginActivity.this, alias, new CustomKeyManager.KeyCallback() {
                            @Override
                            public void onSuccess(CustomKeyManager.KeyEntry entry) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        login();
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                mAccount.setPrivateKeyAlias(null);
                                Timber.e(e, "Failed to load private key");
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                login();
                            }
                        });
                    }
                }
            }
        }, null, null, mAccount.getServerUrl().getHost(), mAccount.getServerUrl().getPort(), null);
    }

    private Callback<UserLogin> mLoginCallback = new Callback<UserLogin>() {

        @Override
        public void onResponse(Call<UserLogin> call, Response<UserLogin> response) {
            if (!response.isSuccessful() || response.body() == null) {
                handleConnectionResponse(response);
                return;
            }

            mAccount.setPrivateToken(response.body().getPrivateToken());
            loadUser();
        }

        @Override
        public void onFailure(Call<UserLogin> call, Throwable t) {
            Timber.e(t, null);
            handleConnectionError(t);
        }
    };

    private Callback<UserFull> mTestUserCallback = new Callback<UserFull>() {

        @Override
        public void onResponse(Call<UserFull> call, Response<UserFull> response) {
            mProgress.setVisibility(View.GONE);
            if (!response.isSuccessful() || response.body() == null) {
                handleConnectionResponse(response);
                return;
            }
            mAccount.setUser(response.body());
            mAccount.setLastUsed(new Date());
            Prefs.addAccount(LoginActivity.this, mAccount);
            GitLabClient.setAccount(mAccount);
            App.bus().post(new LoginEvent(mAccount));
            //This is mostly for if projects already exists, then we will reload the data
            App.bus().post(new ReloadDataEvent());
            Navigator.navigateToStartingActivity(LoginActivity.this);
            finish();
        }

        @Override
        public void onFailure(Call<UserFull> call, Throwable t) {
            Timber.e(t, null);
            handleConnectionError(t);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mTeleprinter = new Teleprinter(this);
        boolean showClose = getIntent().getBooleanExtra(EXTRA_SHOW_CLOSE, false);

        mClose.setVisibility(showClose ? View.VISIBLE : View.GONE);
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
            retrieveAccounts();
        } else {
            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, PERMISSION_REQUEST_GET_ACCOUNTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_GET_ACCOUNTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    retrieveAccounts();
                }
            }
        }
    }

    private void connect(boolean byAuth) {
        mProgress.setVisibility(View.VISIBLE);
        mProgress.setAlpha(0.0f);
        mProgress.animate().alpha(1.0f);

        if (byAuth) {
            connectByAuth();
        } else {
            connectByToken();
        }
    }

    private void connectByAuth() {
        if (mUserInput.getText().toString().contains("@")) {
            GitLabClient.create(mAccount).loginWithEmail(mUserInput.getText().toString(), mPasswordInput.getText().toString()).enqueue(mLoginCallback);
        } else {
            GitLabClient.create(mAccount).loginWithUsername(mUserInput.getText().toString(), mPasswordInput.getText().toString()).enqueue(mLoginCallback);
        }
    }

    private void connectByToken() {
        mAccount.setPrivateToken(mTokenInput.getText().toString());
        loadUser();
    }

    private void loadUser() {
        GitLabClient.create(mAccount).getThisUser().enqueue(mTestUserCallback);
    }

    private void handleConnectionError(Throwable t) {
        mProgress.setVisibility(View.GONE);

        if (t instanceof SSLHandshakeException && t.getCause() instanceof X509CertificateException) {
            mAccount.setTrustedCertificate(null);
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
                                mAccount.setTrustedCertificate(finalFingerprint);
                                login();
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

            ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        } else if (t instanceof SSLPeerUnverifiedException && t.getMessage().toLowerCase().contains("hostname")) {
            mAccount.setTrustedHostname(null);
            final String finalHostname = CustomHostnameVerifier.getLastFailedHostname();
            Dialog d = new AlertDialog.Builder(this)
                    .setTitle(R.string.hostname_title)
                    .setMessage(R.string.hostname_message)
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (finalHostname != null) {
                                mAccount.setTrustedHostname(finalHostname);
                                login();
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

            ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        } else if (t instanceof ConnectException) {
            Snackbar.make(mRoot, t.getLocalizedMessage(), Snackbar.LENGTH_LONG)
                    .show();
        } else {
            Snackbar.make(mRoot, getString(R.string.login_error), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void handleConnectionResponse(Response response) {
        mProgress.setVisibility(View.GONE);
        switch (response.code()) {
            case 401:
                mAccount.setAuthorizationHeader(null);

                String header = response.headers().get("WWW-Authenticate");
                if (header != null) {
                    handleBasicAuthentication(response);
                    return;
                }
                Snackbar.make(mRoot, getString(R.string.login_unauthorized), Snackbar.LENGTH_LONG)
                        .show();
                return;
            case 404:
                Snackbar.make(mRoot, getString(R.string.login_404_error), Snackbar.LENGTH_LONG)
                        .show();
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
                mAccount.setAuthorizationHeader(Credentials.basic(username, password));
                login();
            }

            @Override
            public void onCancel() {
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

    /**
     * Manually retrieve the accounts, typically used for API 23+ after getting the permission. Called automatically
     * on creation, but needs to be recalled if the permission is granted later
     */
    public void retrieveAccounts() {
        Collection<String> accounts = getEmailAccounts();
        if (accounts != null && !accounts.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    R.layout.support_simple_spinner_dropdown_item,
                    new ArrayList<>(accounts));
            mUserInput.setAdapter(adapter);
        }
    }

    /**
     * Get all the accounts that appear to be email accounts. HashSet so that we do not get duplicates
     *
     * @return list of email accounts
     */
    private Set<String> getEmailAccounts() {
        HashSet<String> emailAccounts = new HashSet<>();
        AccountManager manager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        final android.accounts.Account[] accounts = manager.getAccounts();
        for (android.accounts.Account account : accounts) {
            if (!TextUtils.isEmpty(account.name) && Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                emailAccounts.add(account.name);
            }
        }
        return emailAccounts;
    }
}
