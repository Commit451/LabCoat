package com.commit451.gitlab.activity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.commit451.gitlab.R;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Shows user a WebView for login and intercepts the headers to get the private token. Hmmmm
 */
public class WebviewLoginActivity extends BaseActivity {

    @Bind(R.id.webview)
    WebView mWebView;

    OkHttpClient mOkHttpClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_login);
        ButterKnife.bind(this);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new YourWebClient());
        mOkHttpClient = new OkHttpClient();
        mWebView.loadUrl("https://gitlab.com/users/sign_in");
    }

    // this will be the webclient that will manage the webview
    private class YourWebClient extends WebViewClient {

        // you want to catch when an URL is going to be loaded
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.contains("/profile/account")) {
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                mOkHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Timber.e(e, null);
                    }

                    @Override
                    public void onResponse(Call call, final Response response) {
                        try {
                            final String body = response.body().string();
                            if (body.contains(""))
                            mWebView.post(new Runnable() {
                                @Override
                                public void run() {
                                    mWebView.loadData(body, "text/html", "utf-8");
                                }
                            });
                        } catch (Exception e) {
                            Timber.e(e, null);
                        }

                    }
                });
                return true;
            }



            return true;
        }
    }
}
