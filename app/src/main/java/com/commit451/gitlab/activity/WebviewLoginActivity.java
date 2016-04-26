package com.commit451.gitlab.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.commit451.gitlab.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Shows user a WebView for login and intercepts the headers to get the private token. Hmmmm
 */
public class WebviewLoginActivity extends BaseActivity {

    private static final String JAVASCRIPT_INTERFACE_EXTRACTOR = "Extractor";

    private static final String KEY_URL= "url";

    public static Intent newInstance(Context context, String url) {
        Intent intent = new Intent(context, WebviewLoginActivity.class);
        intent.putExtra(KEY_URL, url);
        return intent;
    }

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.webview)
    WebView mWebView;

    @OnClick(R.id.button_get_it)
    void onGetItClicked() {
        mWebView.loadUrl("javascript:" + JAVASCRIPT_INTERFACE_EXTRACTOR + ".showHTML" +
                "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
    }
    String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrl = getIntent().getStringExtra(KEY_URL);
        //TODO remove
        mUrl = "https://gitlab.com/";
        setContentView(R.layout.activity_webview_login);
        ButterKnife.bind(this);

        mToolbar.setTitle("Login -> ");
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new HtmlExtractorJavaScriptInterface(), JAVASCRIPT_INTERFACE_EXTRACTOR);
        mWebView.setWebViewClient(new YourWebClient());
        mWebView.loadUrl(mUrl + "users/sign_in");
    }

    // this will be the webclient that will manage the webview
    private class YourWebClient extends WebViewClient {

        // you want to catch when an URL is going to be loaded
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Timber.d("Loading url %s", url);
            if (url.equals(mUrl.replace("/", ""))) {
                mWebView.loadUrl(mUrl + "/profile/account");
                return true;
            }

            return false;
        }
    }

    private class HtmlExtractorJavaScriptInterface {

        public HtmlExtractorJavaScriptInterface() {
        }

        @JavascriptInterface
        public void showHTML(String html) {
            Timber.d("html" + html);
            //Parse the token out of the HTML
            String startOfToken = "<input type=\"text\" name=\"token\" id=\"token\" value=\"";
            String endOfToken = "\" class=\"form-control\"";

            int indexOfStart = html.indexOf(startOfToken) + startOfToken.length();
            int indexOfEnd = html.indexOf(endOfToken, indexOfStart);
            String token = html.substring(indexOfStart, indexOfEnd);
            Toast.makeText(WebviewLoginActivity.this, "Token:" + token, Toast.LENGTH_LONG)
                    .show();
        }
    }
}
