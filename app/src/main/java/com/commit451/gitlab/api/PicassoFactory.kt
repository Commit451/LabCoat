package com.commit451.gitlab.api

import com.commit451.gitlab.App
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

import okhttp3.OkHttpClient

/**
 * Creates [com.squareup.picasso.Picasso] instances based on the account logged in to
 */
object PicassoFactory {

    fun createPicasso(client: OkHttpClient): Picasso {
        return Picasso.Builder(App.get())
                .downloader(OkHttp3Downloader(client))
                .build()
    }
}
