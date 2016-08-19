package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.commit451.gitlab.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Shows user a WebView for login and intercepts the headers to get the private token. Hmmmm
 */
public class WebviewLoginActivity extends BaseActivity {
    public static final String EXTRA_TOKEN = "token";

    private static final String JAVASCRIPT_INTERFACE_EXTRACTOR = "TokenExtractor";

    private static final String KEY_URL = "url";
    private static final String KEY_EXTRACTING_PRIVATE_TOKEN = "extracting_private_token";

    public static Intent newIntent(Context context, String url, boolean extractingPrivateToken) {
        Intent intent = new Intent(context, WebviewLoginActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_EXTRACTING_PRIVATE_TOKEN, extractingPrivateToken);
        return intent;
    }

    String mUrl;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.progress)
    MaterialProgressBar mProgressBar;
    @BindView(R.id.webview)
    WebView mWebView;

    private final WebChromeClient mWebChromeClient = new WebChromeClient(){
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (mProgressBar.getVisibility() != View.VISIBLE) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mProgressBar.setProgress(newProgress);
            if (newProgress == 100) {
                mProgressBar.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview_login);
        ButterKnife.bind(this);

        mUrl = getIntent().getStringExtra(KEY_URL);
        if (mUrl.endsWith("/")) {
            mUrl = mUrl.substring(0, mUrl.length() - 1);
        }

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new HtmlExtractorJavaScriptInterface(), JAVASCRIPT_INTERFACE_EXTRACTOR);
        mWebView.setWebViewClient(new ExtractionWebClient());
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.clearCache(true);
        mWebView.clearFormData();
        mWebView.clearHistory();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(this);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }

        mWebView.loadUrl(mUrl + "/users/sign_in");
    }

    private boolean isExtracting() {
        return getIntent().getBooleanExtra(KEY_EXTRACTING_PRIVATE_TOKEN, false);
    }

    private class ExtractionWebClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            if (url.equals(mUrl)) {
                if (isExtracting()) {
                    mWebView.loadUrl(mUrl + "/profile/account");
                } else {
                    mWebView.loadUrl(mUrl + "/profile/personal_access_tokens");
                }
                return;
            }

            if (url.equals(mUrl + "/profile/account")) {
                mWebView.loadUrl("javascript:" + JAVASCRIPT_INTERFACE_EXTRACTOR + ".extract" +
                        "(document.getElementById('token').value);");
                return;
            }

            super.onPageFinished(view, url);
        }
    }

    private class HtmlExtractorJavaScriptInterface {

        @JavascriptInterface
        @Keep
        public void extract(String token) {
            Intent data = new Intent();
            data.putExtra(EXTRA_TOKEN, token);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
