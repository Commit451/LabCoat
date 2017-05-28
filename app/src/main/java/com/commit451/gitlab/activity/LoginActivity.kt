package com.commit451.gitlab.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.security.KeyChain
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.Toolbar
import android.text.method.LinkMovementMethod
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnEditorAction
import com.bluelinelabs.logansquare.LoganSquare
import com.commit451.gitlab.App
import com.commit451.gitlab.BuildConfig
import com.commit451.gitlab.R
import com.commit451.gitlab.api.GitLabFactory
import com.commit451.gitlab.api.OkHttpClientFactory
import com.commit451.gitlab.api.request.SessionRequest
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.dialog.HttpLoginDialog
import com.commit451.gitlab.event.LoginEvent
import com.commit451.gitlab.event.ReloadDataEvent
import com.commit451.gitlab.extension.checkValid
import com.commit451.gitlab.extension.setup
import com.commit451.gitlab.extension.text
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.model.api.Message
import com.commit451.gitlab.model.api.UserFull
import com.commit451.gitlab.model.api.UserLogin
import com.commit451.gitlab.navigation.Navigator
import com.commit451.gitlab.rx.CustomResponseSingleObserver
import com.commit451.gitlab.ssl.CustomHostnameVerifier
import com.commit451.gitlab.ssl.CustomKeyManager
import com.commit451.gitlab.ssl.X509CertificateException
import com.commit451.gitlab.ssl.X509Util
import com.commit451.teleprinter.Teleprinter
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.security.cert.CertificateEncodingException
import java.util.*
import java.util.regex.Pattern
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException


class LoginActivity : BaseActivity() {

    companion object {

        private val EXTRA_SHOW_CLOSE = "show_close"

        private val REQUEST_PRIVATE_TOKEN = 123
        private val sTokenPattern = Pattern.compile("^[A-Za-z0-9-_]*$")

        @JvmOverloads fun newIntent(context: Context, showClose: Boolean = false): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(EXTRA_SHOW_CLOSE, showClose)
            return intent
        }
    }

    @BindView(R.id.root) lateinit var root: View
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.url_hint) lateinit var textInputLayoutUrl: TextInputLayout
    @BindView(R.id.url_input) lateinit var textUrl: TextView
    @BindView(R.id.user_input_hint) lateinit var textInputLayoutUser: TextInputLayout
    @BindView(R.id.user_input) lateinit var textUser: EditText
    @BindView(R.id.password_hint) lateinit var textInputLayoutPassword: TextInputLayout
    @BindView(R.id.password_input) lateinit var textPassword: TextView
    @BindView(R.id.token_hint) lateinit var textInputLayoutToken: TextInputLayout
    @BindView(R.id.token_input) lateinit var textToken: TextView
    @BindView(R.id.normal_login) lateinit var rootNormalLogin: View
    @BindView(R.id.token_login) lateinit var rootTokenLogin: View
    @BindView(R.id.progress) lateinit var progress: View

    lateinit var teleprinter: Teleprinter

    var isNormalLogin = true
    val emailPattern : Pattern by lazy {
        Patterns.EMAIL_ADDRESS
    }
    var account: Account = Account()

    @OnEditorAction(R.id.password_input, R.id.token_input)
    fun onPasswordEditorAction(): Boolean {
        onLoginClick()
        return true
    }

    @OnClick(R.id.login_button)
    fun onLoginClick() {
        teleprinter.hideKeyboard()

        if (!textInputLayoutUrl.checkValid()) {
            return
        }

        if (!verifyUrl()) {
            return
        }
        val uri = Uri.parse(textInputLayoutUrl.editText!!.text.toString())

        if (isNormalLogin) {
            val valid = textInputLayoutUser.checkValid() and textInputLayoutPassword.checkValid()
            if (!valid) {
                return
            }
        } else {
            if (!textInputLayoutToken.checkValid()) {
                return
            }
            if (!sTokenPattern.matcher(textToken.text).matches()) {
                textInputLayoutToken.error = getString(R.string.not_a_valid_private_token)
                return
            } else {
                textInputLayoutToken.error = null
            }
        }

        if (isAlreadySignedIn(uri.toString(), if (isNormalLogin) textUser.text.toString() else textToken.text.toString())) {
            Snackbar.make(root, getString(R.string.already_logged_in), Snackbar.LENGTH_LONG)
                    .show()
            return
        }

        account = Account()
        account.serverUrl = uri

        login()
    }

    @OnClick(R.id.button_open_login_page)
    fun onOpenLoginPageClicked() {
        if (verifyUrl()) {
            val url = textInputLayoutUrl.editText!!.text.toString()
            Navigator.navigateToWebSignin(this, url, true, REQUEST_PRIVATE_TOKEN)
        }
    }

    @OnClick(R.id.button_open_login_page_for_personal_access)
    fun onOpenLoginPageForPersonalAccessTokenClicked() {
        if (verifyUrl()) {
            val url = textInputLayoutUrl.editText!!.text.toString()
            Navigator.navigateToWebSignin(this, url, false, REQUEST_PRIVATE_TOKEN)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)

        teleprinter = Teleprinter(this)
        val showClose = intent.getBooleanExtra(EXTRA_SHOW_CLOSE, false)

        toolbar.inflateMenu(R.menu.advanced_login)
        toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_advanced_login -> {
                    val isNormalLogin = rootNormalLogin.visibility == View.VISIBLE
                    if (isNormalLogin) {
                        rootNormalLogin.visibility = View.GONE
                        rootTokenLogin.visibility = View.VISIBLE
                        item.setTitle(R.string.normal_link)
                        this@LoginActivity.isNormalLogin = false
                    } else {
                        rootNormalLogin.visibility = View.VISIBLE
                        rootTokenLogin.visibility = View.GONE
                        item.setTitle(R.string.advanced_login)
                        this@LoginActivity.isNormalLogin = true
                    }
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        if (showClose) {
            toolbar.setNavigationIcon(R.drawable.ic_close_24dp)
            toolbar.setNavigationOnClickListener { onBackPressed() }
        }

        textUrl.setText(R.string.url_gitlab)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_PRIVATE_TOKEN -> if (resultCode == Activity.RESULT_OK) {
                val token = data?.getStringExtra(WebLoginActivity.EXTRA_TOKEN)
                textInputLayoutToken.editText!!.setText(token)
            }
        }
    }

    fun connect(byAuth: Boolean) {
        progress.visibility = View.VISIBLE
        progress.alpha = 0.0f
        progress.animate().alpha(1.0f)

        if (byAuth) {
            connectByAuth()
        } else {
            connectByToken()
        }
    }

    fun connectByAuth() {
        val request = SessionRequest()
        request.setPassword(textInputLayoutPassword.text())
        val usernameOrEmail = textInputLayoutUser.text()
        if (emailPattern.matcher(usernameOrEmail).matches()) {
            request.setEmail(usernameOrEmail)
        } else {
            request.setLogin(usernameOrEmail)
        }
        attemptLogin(request)
    }

    fun attemptLogin(request: SessionRequest) {
        val gitlabClientBuilder = OkHttpClientFactory.create(account)
        if (BuildConfig.DEBUG) {
            gitlabClientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        val gitLab = GitLabFactory.create(account, gitlabClientBuilder.build())
        gitLab.login(request)
                .setup(bindToLifecycle())
                .subscribe(object : CustomResponseSingleObserver<UserLogin>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        if (e is HttpException) {
                            handleConnectionResponse(response())
                        } else {
                            handleConnectionError(e)
                        }
                    }

                    override fun responseNonNullSuccess(userLogin: UserLogin) {
                        account.privateToken = userLogin.privateToken
                        loadUser()
                    }
                })
    }

    fun connectByToken() {
        account.privateToken = textToken.text.toString()
        loadUser()
    }

    fun loginWithPrivateToken() {
        KeyChain.choosePrivateKeyAlias(this, { alias ->
            account.privateKeyAlias = alias

            if (alias != null) {
                if (!CustomKeyManager.isCached(alias)) {
                    CustomKeyManager.cache(this@LoginActivity, alias, object : CustomKeyManager.KeyCallback {
                        override fun onSuccess(entry: CustomKeyManager.KeyEntry) {
                            runOnUiThread { login() }
                        }

                        override fun onError(e: Exception) {
                            account.privateKeyAlias = null
                            Timber.e(e, "Failed to load private key")
                        }
                    })
                } else {
                    runOnUiThread { login() }
                }
            }
        }, null, null, account.serverUrl.host, account.serverUrl.port, null)
    }

    fun verifyUrl(): Boolean {
        val url = textUrl.text.toString()
        var uri: Uri? = null
        try {
            if (HttpUrl.parse(url) != null) {
                uri = Uri.parse(url)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        if (uri == null) {
            textInputLayoutUrl.error = getString(R.string.not_a_valid_url)
            return false
        } else {
            textInputLayoutUrl.error = null
        }
        if (url[url.length - 1] != '/') {
            textInputLayoutUrl.error = getString(R.string.please_end_your_url_with_a_slash)
            return false
        } else {
            textInputLayoutUrl.error = null
        }
        return true
    }

    fun login() {
        // This seems useless - But believe me, it makes everything work! Don't remove it.
        // (OkHttpClientFactory caches the clients and needs a new account to recreate them)

        val newAccount = Account()
        newAccount.serverUrl = account.serverUrl
        newAccount.trustedCertificate = account.trustedCertificate
        newAccount.trustedHostname = account.trustedHostname
        newAccount.privateKeyAlias = account.privateKeyAlias
        newAccount.authorizationHeader = account.authorizationHeader
        account = newAccount

        if (isNormalLogin) {
            connect(true)
        } else {
            connect(false)
        }
    }

    fun loadUser() {
        val gitlabClientBuilder = OkHttpClientFactory.create(account, false)
        if (BuildConfig.DEBUG) {
            gitlabClientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        val gitLab = GitLabFactory.create(account, gitlabClientBuilder.build())
        gitLab.getThisUser()
                .setup(bindToLifecycle())
                .subscribe(object : CustomResponseSingleObserver<UserFull>() {

                    override fun error(e: Throwable) {
                        Timber.e(e)
                        if (e is HttpException) {
                            handleConnectionResponse(response())
                        } else {
                            handleConnectionError(e)
                        }
                    }

                    override fun responseNonNullSuccess(userFull: UserFull) {
                        progress.visibility = View.GONE
                        account.user = userFull
                        account.lastUsed = Date()
                        Prefs.addAccount(account)
                        App.get().setAccount(account)
                        App.bus().post(LoginEvent(account))
                        //This is mostly for if projects already exists, then we will reload the data
                        App.bus().post(ReloadDataEvent())
                        Navigator.navigateToStartingActivity(this@LoginActivity)
                        finish()
                    }
                })
    }

    fun handleConnectionError(t: Throwable) {
        progress.visibility = View.GONE

        if (t is SSLHandshakeException && t.cause is X509CertificateException) {
            account.trustedCertificate = null
            var fingerprint: String? = null
            try {
                fingerprint = X509Util.getFingerPrint((t.cause as X509CertificateException).chain[0])
            } catch (e: CertificateEncodingException) {
                Timber.e(e)
            }

            val finalFingerprint = fingerprint

            val d = AlertDialog.Builder(this)
                    .setTitle(R.string.certificate_title)
                    .setMessage(String.format(resources.getString(R.string.certificate_message), finalFingerprint))
                    .setPositiveButton(R.string.ok_button) { dialog, which ->
                        if (finalFingerprint != null) {
                            account.trustedCertificate = finalFingerprint
                            login()
                        }

                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.cancel_button) { dialog, which -> dialog.dismiss() }
                    .show()

            (d.findViewById(android.R.id.message) as TextView).movementMethod = LinkMovementMethod.getInstance()
        } else if (t is SSLPeerUnverifiedException && t.message?.toLowerCase()!!.contains("hostname")) {
            account.trustedHostname = null
            val finalHostname = CustomHostnameVerifier.lastFailedHostname
            val d = AlertDialog.Builder(this)
                    .setTitle(R.string.hostname_title)
                    .setMessage(R.string.hostname_message)
                    .setPositiveButton(R.string.ok_button) { dialog, which ->
                        if (finalHostname != null) {
                            account.trustedHostname = finalHostname
                            login()
                        }

                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.cancel_button) { dialog, which -> dialog.dismiss() }
                    .show()

            (d.findViewById(android.R.id.message) as TextView).movementMethod = LinkMovementMethod.getInstance()
        } else if (t is ConnectException) {
            Snackbar.make(root, t.message!!, Snackbar.LENGTH_LONG)
                    .show()
        } else {
            Snackbar.make(root, getString(R.string.login_error), Snackbar.LENGTH_LONG)
                    .show()
        }
    }

    fun handleConnectionResponse(response: Response<*>) {
        progress.visibility = View.GONE
        when (response.code()) {
            401 -> {
                account.authorizationHeader = null

                val header = response.headers().get("WWW-Authenticate")
                if (header != null) {
                    handleBasicAuthentication(response)
                    return
                }
                var errorMessage = getString(R.string.login_unauthorized)
                try {
                    val message = LoganSquare.parse(response.errorBody()!!.byteStream(), Message::class.java)
                    if (message != null && message.message != null) {
                        errorMessage = message.message
                    }
                } catch (e: IOException) {
                    Timber.e(e)
                }

                Snackbar.make(root, errorMessage, Snackbar.LENGTH_LONG)
                        .show()
                return
            }
            404 -> {
                Snackbar.make(root, getString(R.string.login_404_error), Snackbar.LENGTH_LONG)
                        .show()
                Snackbar.make(root, getString(R.string.login_error), Snackbar.LENGTH_LONG)
                        .show()
            }
            else -> Snackbar.make(root, getString(R.string.login_error), Snackbar.LENGTH_LONG).show()
        }
    }

    fun handleBasicAuthentication(response: Response<*>) {
        val header = response.headers().get("WWW-Authenticate")!!.trim { it <= ' ' }
        if (!header.startsWith("Basic")) {
            Snackbar.make(root, getString(R.string.login_unsupported_authentication), Snackbar.LENGTH_LONG)
                    .show()
            return
        }

        val realmStart = header.indexOf('"') + 1
        val realmEnd = header.lastIndexOf('"')
        var realm = ""
        if (realmStart > 0 && realmEnd > -1) {
            realm = header.substring(realmStart, realmEnd)
        }

        val dialog = HttpLoginDialog(this, realm, object : HttpLoginDialog.LoginListener {
            override fun onLogin(username: String, password: String) {
                account.authorizationHeader = Credentials.basic(username, password)
                login()
            }

            override fun onCancel() {}
        })
        dialog.show()
    }

    fun isAlreadySignedIn(url: String, usernameOrEmailOrPrivateToken: String): Boolean {
        val accounts = Prefs.getAccounts()
        for (account in accounts) {
            if (account.serverUrl == Uri.parse(url)) {
                if (usernameOrEmailOrPrivateToken == account.user.username
                        || usernameOrEmailOrPrivateToken.equals(account.user.email, ignoreCase = true)
                        || usernameOrEmailOrPrivateToken.equals(account.privateToken, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }
}
