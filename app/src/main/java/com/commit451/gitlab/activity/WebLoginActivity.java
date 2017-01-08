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
public class WebLoginActivity extends BaseActivity {
    public static final String EXTRA_TOKEN = "token";

    private static final String JAVASCRIPT_INTERFACE_EXTRACTOR = "TokenExtractor";

    /**
     * This is pretty fragile and has changed in the past, so if this screen ever stops working,
     * it's probably due to this id changing. Go inspect the source of the page to verify
     */
    private static final String PRIVATE_TOKEN_HTML_ID = "private-token";

    private static final String KEY_URL = "url";
    private static final String KEY_EXTRACTING_PRIVATE_TOKEN = "extracting_private_token";

    public static Intent newIntent(Context context, String url, boolean extractingPrivateToken) {
        Intent intent = new Intent(context, WebLoginActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_EXTRACTING_PRIVATE_TOKEN, extractingPrivateToken);
        return intent;
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.progress)
    MaterialProgressBar progress;
    @BindView(R.id.webview)
    WebView webView;

    String url;

    private final WebChromeClient webChromeClient = new WebChromeClient(){
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (progress.getVisibility() != View.VISIBLE) {
                progress.setVisibility(View.VISIBLE);
            }
            progress.setProgress(newProgress);
            if (newProgress == 100) {
                progress.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview_login);
        ButterKnife.bind(this);

        url = getIntent().getStringExtra(KEY_URL);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        toolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new HtmlExtractorJavaScriptInterface(), JAVASCRIPT_INTERFACE_EXTRACTOR);
        webView.setWebViewClient(new ExtractionWebClient());
        webView.setWebChromeClient(webChromeClient);
        webView.clearCache(true);
        webView.clearFormData();
        webView.clearHistory();

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

        webView.loadUrl(url + "/users/sign_in");
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

            if (url.equals(WebLoginActivity.this.url)) {
                if (isExtracting()) {
                    webView.loadUrl(WebLoginActivity.this.url + "/profile/account");
                } else {
                    webView.loadUrl(WebLoginActivity.this.url + "/profile/personal_access_tokens");
                }
                return;
            }

            if (url.equals(WebLoginActivity.this.url + "/profile/account")) {
                webView.loadUrl("javascript:" + JAVASCRIPT_INTERFACE_EXTRACTOR + ".extract" +
                        "(document.getElementById('" + PRIVATE_TOKEN_HTML_ID + "').value);");
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
