package com.commit451.gitlab.api;

import com.commit451.gitlab.App;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;

/**
 * Creates {@link com.squareup.picasso.Picasso} instances based on the account logged in to
 */
public class PicassoFactory {

    public static Picasso createPicasso(OkHttpClient client) {
        return new Picasso.Builder(App.get())
                .downloader(new OkHttp3Downloader(client))
                .build();
    }
}
