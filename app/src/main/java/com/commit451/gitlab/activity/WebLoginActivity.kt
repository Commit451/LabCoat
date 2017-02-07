package com.commit451.gitlab.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.Keep
import android.support.v7.widget.Toolbar
import android.view.View
import android.webkit.*
import butterknife.BindView
import butterknife.ButterKnife
import com.commit451.gitlab.R
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

/**
 * Shows user a WebView for login and intercepts the headers to get the private token. Hmmmm
 */
class WebLoginActivity : BaseActivity() {

    companion object {
        val EXTRA_TOKEN = "token"

        private val JAVASCRIPT_INTERFACE_EXTRACTOR = "TokenExtractor"

        /**
         * This is pretty fragile and has changed in the past, so if this screen ever stops working,
         * it's probably due to this id changing. Go inspect the source of the page to verify
         */
        private val PRIVATE_TOKEN_HTML_ID = "private-token"

        private val KEY_URL = "url"
        private val KEY_EXTRACTING_PRIVATE_TOKEN = "extracting_private_token"

        fun newIntent(context: Context, url: String, extractingPrivateToken: Boolean): Intent {
            val intent = Intent(context, WebLoginActivity::class.java)
            intent.putExtra(KEY_URL, url)
            intent.putExtra(KEY_EXTRACTING_PRIVATE_TOKEN, extractingPrivateToken)
            return intent
        }
    }

    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.progress) lateinit var progress: MaterialProgressBar
    @BindView(R.id.webview) lateinit var webView: WebView

    lateinit var url: String

    val webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (progress.visibility != View.VISIBLE) {
                progress.visibility = View.VISIBLE
            }
            progress.progress = newProgress
            if (newProgress == 100) {
                progress.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_webview_login)
        ButterKnife.bind(this)

        url = intent.getStringExtra(KEY_URL)
        if (url.endsWith("/")) {
            url = url.substring(0, url.length - 1)
        }

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val settings = webView.settings
        settings.javaScriptEnabled = true
        webView.addJavascriptInterface(HtmlExtractorJavaScriptInterface(), JAVASCRIPT_INTERFACE_EXTRACTOR)
        webView.setWebViewClient(ExtractionWebClient())
        webView.setWebChromeClient(webChromeClient)
        webView.clearCache(true)
        webView.clearFormData()
        webView.clearHistory()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        } else {
            val cookieSyncMngr = CookieSyncManager.createInstance(this)
            cookieSyncMngr.startSync()
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookie()
            cookieManager.removeSessionCookie()
            cookieSyncMngr.stopSync()
            cookieSyncMngr.sync()
        }

        webView.loadUrl(url + "/users/sign_in")
    }

    val isExtracting: Boolean
        get() = intent.getBooleanExtra(KEY_EXTRACTING_PRIVATE_TOKEN, false)

    inner class ExtractionWebClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            var modifiedUrl = url
            if (modifiedUrl.endsWith("/")) {
                modifiedUrl = url.substring(0, url.length - 1)
            }

            if (modifiedUrl == this@WebLoginActivity.url) {
                if (isExtracting) {
                    webView.loadUrl(this@WebLoginActivity.url + "/profile/account")
                } else {
                    webView.loadUrl(this@WebLoginActivity.url + "/profile/personal_access_tokens")
                }
                return
            }

            if (modifiedUrl == this@WebLoginActivity.url + "/profile/account") {
                webView.loadUrl("javascript:" + JAVASCRIPT_INTERFACE_EXTRACTOR + ".extract" +
                        "(document.getElementById('" + PRIVATE_TOKEN_HTML_ID + "').value);")
                return
            }

            super.onPageFinished(view, url)
        }
    }

    inner class HtmlExtractorJavaScriptInterface {

        @JavascriptInterface
        @Keep
        fun extract(token: String) {
            val data = Intent()
            data.putExtra(EXTRA_TOKEN, token)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}
