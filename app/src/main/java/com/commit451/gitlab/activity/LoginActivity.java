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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bluelinelabs.logansquare.LoganSquare;
import com.commit451.gitlab.App;
import com.commit451.gitlab.BuildConfig;
import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLab;
import com.commit451.gitlab.api.GitLabFactory;
import com.commit451.gitlab.api.OkHttpClientFactory;
import com.commit451.gitlab.dialog.HttpLoginDialog;
import com.commit451.gitlab.event.LoginEvent;
import com.commit451.gitlab.event.ReloadDataEvent;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.model.api.Message;
import com.commit451.gitlab.model.api.UserFull;
import com.commit451.gitlab.model.api.UserLogin;
import com.commit451.gitlab.navigation.Navigator;
import com.commit451.gitlab.rx.CustomResponseSingleObserver;
import com.commit451.gitlab.ssl.CustomHostnameVerifier;
import com.commit451.gitlab.ssl.CustomKeyManager;
import com.commit451.gitlab.ssl.X509CertificateException;
import com.commit451.gitlab.ssl.X509Util;
import com.commit451.teleprinter.Teleprinter;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.io.IOException;
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
import butterknife.OnEditorAction;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import timber.log.Timber;


public class LoginActivity extends BaseActivity {

    private static final String EXTRA_SHOW_CLOSE = "show_close";

    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1337;
    private static final int REQUEST_PRIVATE_TOKEN = 123;
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
    View root;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.url_hint)
    TextInputLayout textInputLayoutUrl;
    @BindView(R.id.url_input)
    TextView textUrl;
    @BindView(R.id.user_input_hint)
    TextInputLayout textInputLayoutUser;
    @BindView(R.id.user_input)
    AppCompatAutoCompleteTextView textUser;
    @BindView(R.id.password_hint)
    TextInputLayout textInputLayoutPassword;
    @BindView(R.id.password_input)
    TextView textPassword;
    @BindView(R.id.token_hint)
    TextInputLayout textInputLayoutToken;
    @BindView(R.id.token_input)
    TextView textToken;
    @BindView(R.id.normal_login)
    View rootNormalLogin;
    @BindView(R.id.token_login)
    View rootTokenLogin;
    @BindView(R.id.progress)
    View progress;

    Teleprinter teleprinter;

    boolean isNormalLogin = true;
    Account account;

    @OnEditorAction({R.id.password_input, R.id.token_input})
    boolean onPasswordEditorAction() {
        onLoginClick();
        return true;
    }

    @OnClick(R.id.login_button)
    public void onLoginClick() {
        teleprinter.hideKeyboard();

        if (hasEmptyFields(textInputLayoutUrl)) {
            return;
        }

        if (!verifyUrl()) {
            return;
        }
        Uri uri = Uri.parse(textInputLayoutUrl.getEditText().getText().toString());

        if (isNormalLogin) {
            if (hasEmptyFields(textInputLayoutUser, textInputLayoutPassword)) {
                return;
            }
        } else {
            if (hasEmptyFields(textInputLayoutToken)) {
                return;
            }
            if (!sTokenPattern.matcher(textToken.getText()).matches()) {
                textInputLayoutToken.setError(getString(R.string.not_a_valid_private_token));
                return;
            } else {
                textInputLayoutToken.setError(null);
            }
        }

        if (isAlreadySignedIn(uri.toString(), isNormalLogin ? textUser.getText().toString() : textToken.getText().toString())) {
            Snackbar.make(root, getString(R.string.already_logged_in), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        account = new Account();
        account.setServerUrl(uri);

        login();
    }

    @OnClick(R.id.button_open_login_page)
    void onOpenLoginPageClicked() {
        if (verifyUrl()) {
            String url = textInputLayoutUrl.getEditText().getText().toString();
            Navigator.navigateToWebSignin(this, url, true, REQUEST_PRIVATE_TOKEN);
        }
    }

    @OnClick(R.id.button_open_login_page_for_personal_access)
    void onOpenLoginPageForPersonalAccessTokenClicked() {
        if (verifyUrl()) {
            String url = textInputLayoutUrl.getEditText().getText().toString();
            Navigator.navigateToWebSignin(this, url, false, REQUEST_PRIVATE_TOKEN);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        teleprinter = new Teleprinter(this);
        boolean showClose = getIntent().getBooleanExtra(EXTRA_SHOW_CLOSE, false);

        toolbar.inflateMenu(R.menu.menu_login);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_advanced_login:
                        boolean isNormalLogin = rootNormalLogin.getVisibility() == View.VISIBLE;
                        if (isNormalLogin) {
                            rootNormalLogin.setVisibility(View.GONE);
                            rootTokenLogin.setVisibility(View.VISIBLE);
                            item.setTitle(R.string.normal_link);
                            LoginActivity.this.isNormalLogin = false;
                        } else {
                            rootNormalLogin.setVisibility(View.VISIBLE);
                            rootTokenLogin.setVisibility(View.GONE);
                            item.setTitle(R.string.advanced_login);
                            LoginActivity.this.isNormalLogin = true;
                        }
                        return true;
                }
                return false;
            }
        });
        if (showClose) {
            toolbar.setNavigationIcon(R.drawable.ic_close_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }

        textUser.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    checkAccountPermission();
                }
            }
        });
        textUrl.setText(R.string.url_gitlab);
    }

    @TargetApi(23)
    private void checkAccountPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
            retrieveAccounts();
        } else {
            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, REQUEST_PERMISSION_GET_ACCOUNTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_GET_ACCOUNTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    retrieveAccounts();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PRIVATE_TOKEN:
                if (resultCode == RESULT_OK) {
                    String token = data.getStringExtra(WebLoginActivity.EXTRA_TOKEN);
                    textInputLayoutToken.getEditText().setText(token);
                }
                break;
        }
    }

    private void connect(boolean byAuth) {
        progress.setVisibility(View.VISIBLE);
        progress.setAlpha(0.0f);
        progress.animate().alpha(1.0f);

        if (byAuth) {
            connectByAuth();
        } else {
            connectByToken();
        }
    }

    private void connectByAuth() {
        OkHttpClient.Builder gitlabClientBuilder = OkHttpClientFactory.create(account);
        if (BuildConfig.DEBUG) {
            gitlabClientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        GitLab gitLab = GitLabFactory.create(account, gitlabClientBuilder.build());
        if (textUser.getText().toString().contains("@")) {
            attemptLogin(gitLab.loginWithEmail(textUser.getText().toString(), textPassword.getText().toString()));
        } else {
            attemptLogin(gitLab.loginWithEmail(textUser.getText().toString(), textPassword.getText().toString()));
        }
    }

    private void attemptLogin(Single<Response<UserLogin>> observable) {
        observable
                .compose(this.<Response<UserLogin>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<UserLogin>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        if (e instanceof HttpException) {
                            handleConnectionResponse(response());
                        } else {
                            handleConnectionError(e);
                        }
                    }

                    @Override
                    public void responseSuccess(@NonNull UserLogin userLogin) {
                        account.setPrivateToken(userLogin.getPrivateToken());
                        loadUser();
                    }
                });
    }

    private void connectByToken() {
        account.setPrivateToken(textToken.getText().toString());
        loadUser();
    }

    private void loginWithPrivateToken() {
        KeyChain.choosePrivateKeyAlias(this, new KeyChainAliasCallback() {
            @Override
            public void alias(String alias) {
                account.setPrivateKeyAlias(alias);

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
                                account.setPrivateKeyAlias(null);
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
        }, null, null, account.getServerUrl().getHost(), account.getServerUrl().getPort(), null);
    }

    private boolean verifyUrl() {
        String url = textUrl.getText().toString();
        Uri uri = null;
        try {
            if (HttpUrl.parse(url) != null) {
                uri = Uri.parse(url);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        if (uri == null) {
            textInputLayoutUrl.setError(getString(R.string.not_a_valid_url));
            return false;
        } else {
            textInputLayoutUrl.setError(null);
        }
        if (url.charAt(url.length() - 1) != '/') {
            textInputLayoutUrl.setError(getString(R.string.please_end_your_url_with_a_slash));
            return false;
        } else {
            textInputLayoutUrl.setError(null);
        }
        return true;
    }

    private void login() {
        // This seems useless - But believe me, it makes everything work! Don't remove it.
        // (OkHttpClientFactory caches the clients and needs a new account to recreate them)

        Account newAccount = new Account();
        newAccount.setServerUrl(account.getServerUrl());
        newAccount.setTrustedCertificate(account.getTrustedCertificate());
        newAccount.setTrustedHostname(account.getTrustedHostname());
        newAccount.setPrivateKeyAlias(account.getPrivateKeyAlias());
        newAccount.setAuthorizationHeader(account.getAuthorizationHeader());
        account = newAccount;

        if (isNormalLogin) {
            connect(true);
        } else {
            connect(false);
        }
    }

    private void loadUser() {
        OkHttpClient.Builder gitlabClientBuilder = OkHttpClientFactory.create(account, false);
        if (BuildConfig.DEBUG) {
            gitlabClientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        GitLab gitLab = GitLabFactory.create(account, gitlabClientBuilder.build());
        gitLab.getThisUser()
                .compose(this.<Response<UserFull>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CustomResponseSingleObserver<UserFull>() {

                    @Override
                    public void error(@NonNull Throwable e) {
                        Timber.e(e);
                        if (e instanceof HttpException) {
                            handleConnectionResponse(response());
                        } else {
                            handleConnectionError(e);
                        }
                    }

                    @Override
                    public void responseSuccess(@NonNull UserFull userFull) {
                        progress.setVisibility(View.GONE);
                        account.setUser(userFull);
                        account.setLastUsed(new Date());
                        App.get().getPrefs().addAccount(account);
                        App.get().setAccount(account);
                        App.bus().post(new LoginEvent(account));
                        //This is mostly for if projects already exists, then we will reload the data
                        App.bus().post(new ReloadDataEvent());
                        Navigator.navigateToStartingActivity(LoginActivity.this);
                        finish();
                    }
                });
    }

    private void handleConnectionError(Throwable t) {
        progress.setVisibility(View.GONE);

        if (t instanceof SSLHandshakeException && t.getCause() instanceof X509CertificateException) {
            account.setTrustedCertificate(null);
            String fingerprint = null;
            try {
                fingerprint = X509Util.getFingerPrint(((X509CertificateException) t.getCause()).getChain()[0]);
            } catch (CertificateEncodingException e) {
                Timber.e(e);
            }
            final String finalFingerprint = fingerprint;

            Dialog d = new AlertDialog.Builder(this)
                    .setTitle(R.string.certificate_title)
                    .setMessage(String.format(getResources().getString(R.string.certificate_message), finalFingerprint))
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (finalFingerprint != null) {
                                account.setTrustedCertificate(finalFingerprint);
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
            account.setTrustedHostname(null);
            final String finalHostname = CustomHostnameVerifier.getLastFailedHostname();
            Dialog d = new AlertDialog.Builder(this)
                    .setTitle(R.string.hostname_title)
                    .setMessage(R.string.hostname_message)
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (finalHostname != null) {
                                account.setTrustedHostname(finalHostname);
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
            Snackbar.make(root, t.getLocalizedMessage(), Snackbar.LENGTH_LONG)
                    .show();
        } else {
            Snackbar.make(root, getString(R.string.login_error), Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void handleConnectionResponse(Response response) {
        progress.setVisibility(View.GONE);
        switch (response.code()) {
            case 401:
                account.setAuthorizationHeader(null);

                String header = response.headers().get("WWW-Authenticate");
                if (header != null) {
                    handleBasicAuthentication(response);
                    return;
                }
                String errorMessage = getString(R.string.login_unauthorized);
                try {
                    Message message = LoganSquare.parse(response.errorBody().byteStream(), Message.class);
                    if (message != null && message.getMessage() != null) {
                        errorMessage = message.getMessage();
                    }
                } catch (IOException e) {
                    Timber.e(e);
                }
                Snackbar.make(root, errorMessage, Snackbar.LENGTH_LONG)
                        .show();
                return;
            case 404:
                Snackbar.make(root, getString(R.string.login_404_error), Snackbar.LENGTH_LONG)
                        .show();
            default:
                Snackbar.make(root, getString(R.string.login_error), Snackbar.LENGTH_LONG)
                        .show();
        }
    }

    private void handleBasicAuthentication(Response response) {
        String header = response.headers().get("WWW-Authenticate").trim();
        if (!header.startsWith("Basic")) {
            Snackbar.make(root, getString(R.string.login_unsupported_authentication), Snackbar.LENGTH_LONG)
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
                account.setAuthorizationHeader(Credentials.basic(username, password));
                login();
            }

            @Override
            public void onCancel() {
            }
        });
        dialog.show();
    }

    private boolean isAlreadySignedIn(@NonNull String url, @NonNull String usernameOrEmailOrPrivateToken) {
        List<Account> accounts = App.get().getPrefs().getAccounts();
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
    private void retrieveAccounts() {
        Collection<String> accounts = getEmailAccounts();
        if (accounts != null && !accounts.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    R.layout.support_simple_spinner_dropdown_item,
                    new ArrayList<>(accounts));
            textUser.setAdapter(adapter);
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
        @SuppressWarnings("MissingPermission")
        final android.accounts.Account[] accounts = manager.getAccounts();
        for (android.accounts.Account account : accounts) {
            if (!TextUtils.isEmpty(account.name) && Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                emailAccounts.add(account.name);
            }
        }
        return emailAccounts;
    }
}
