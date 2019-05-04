package com.commit451.gitlab.extension

import android.net.Uri
import android.widget.ImageView
import com.commit451.gitlab.App

fun ImageView.load(url: String) {
    App.get().picasso
        .load(url)
        .into(this)
}

fun ImageView.load(uri: Uri) {
    App.get().picasso
        .load(uri)
        .into(this)
}
