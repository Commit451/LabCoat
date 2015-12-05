package com.commit451.gitlab.api;

import com.commit451.gitlab.BuildConfig;
import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.ssl.CustomTrustManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.joda.time.format.ISODateTimeFormat;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import android.net.Uri;

import java.lang.reflect.Type;
import java.util.Date;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;

/**
 * Pulls all the GitLab stuff from the API
 * Created by Jawn on 7/28/2015.
 */
public class GitLabClient {

    private static GitLab sGitLab;
    private static GitLabRss sGitLabRss;
    private static Picasso sPicasso;
    private static CustomTrustManager sCustomTrustManager = new CustomTrustManager();

    public static GitLab instance() {
        if (sGitLab == null) {
            // Configure Gson to handle dates correctly
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                @Override
                public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    if (json.isJsonNull()) {
                        return null;
                    }

                    return convertDate(json.getAsString());
                }
            });
            gsonBuilder.registerTypeAdapter(Uri.class, new JsonDeserializer<Uri>() {
                @Override
                public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    if (json.isJsonNull()) {
                        return null;
                    }

                    return convertUri(json.getAsString());
                }
            });
            Gson gson = gsonBuilder.create();

            OkHttpClient client = new OkHttpClient();
            client.setSslSocketFactory(sCustomTrustManager.getSSLSocketFactory());
            client.interceptors().add(new PrivateTokenRequestInterceptor(true));
            if (BuildConfig.DEBUG) {
                client.networkInterceptors().add(new TimberRequestInterceptor());
            }

            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl(Prefs.getServerUrl(GitLabApp.instance()))
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            sGitLab = restAdapter.create(GitLab.class);
        }

        return sGitLab;
    }

    public static GitLabRss rssInstance() {
        if (sGitLabRss == null) {
            OkHttpClient client = new OkHttpClient();
            client.setSslSocketFactory(sCustomTrustManager.getSSLSocketFactory());
            client.interceptors().add(new PrivateTokenRequestInterceptor(false));
            if (BuildConfig.DEBUG) {
                client.networkInterceptors().add(new TimberRequestInterceptor());
            }

            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl(Prefs.getServerUrl(GitLabApp.instance()))
                    .addConverterFactory(SimpleXmlConverterFactory.create(new Persister(new Matcher() {
                        @Override
                        public Transform match(Class type) throws Exception {
                            if (Date.class.equals(type)) {
                                return new Transform<Date>() {
                                    @Override
                                    public Date read(String value) throws Exception {
                                        return convertDate(value);
                                    }

                                    @Override
                                    public String write(Date value) throws Exception {
                                        throw new UnsupportedOperationException();
                                    }
                                };
                            } else if (Uri.class.equals(type)) {
                                return new Transform<Uri>() {
                                    @Override
                                    public Uri read(String value) throws Exception {
                                        return convertUri(value);
                                    }

                                    @Override
                                    public String write(Uri value) throws Exception {
                                        throw new UnsupportedOperationException();
                                    }
                                };
                            }

                            return null;
                        }
                    })))
                    .client(client)
                    .build();
            sGitLabRss = restAdapter.create(GitLabRss.class);
        }

        return sGitLabRss;
    }

    public static Picasso getPicasso() {
        if (sPicasso == null) {
            OkHttpClient client = new OkHttpClient();
            client.setSslSocketFactory(sCustomTrustManager.getSSLSocketFactory());
            client.interceptors().add(new PrivateTokenRequestInterceptor(false));
            if (BuildConfig.DEBUG) {
                client.networkInterceptors().add(new TimberRequestInterceptor());
            }

            sPicasso = new Picasso.Builder(GitLabApp.instance())
                    .downloader(new OkHttpDownloader(client))
                    .build();
        }

        return sPicasso;
    }

    public static void reset() {
        sGitLab = null;
        sGitLabRss = null;
        sPicasso = null;
    }

    public static void setTrustedCertificate(String trustedCertificate) {
        sCustomTrustManager.setTrustedCertificate(trustedCertificate);
    }

    private static Date convertDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        return ISODateTimeFormat.dateTimeParser().parseDateTime(dateString).toDate();
    }

    private static Uri convertUri(String uriString) {
        if (uriString == null) {
            return null;
        }
        if (uriString.isEmpty()) {
            return Uri.EMPTY;
        }

        Uri uri = Uri.parse(uriString);
        if (!uri.isRelative()) {
            return uri;
        }

        Uri.Builder builder = Uri.parse(Prefs.getServerUrl(GitLabApp.instance()))
                .buildUpon()
                .encodedQuery(uri.getEncodedQuery())
                .encodedFragment(uri.getEncodedFragment());

        if (uri.getPath().startsWith("/")) {
            builder.encodedPath(uri.getEncodedPath());
        } else {
            builder.appendEncodedPath(uri.getEncodedPath());
        }

        return builder.build();
    }
}
