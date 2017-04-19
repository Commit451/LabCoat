package com.commit451.gitlab.extension

import android.os.Bundle

//Bundle extensions

fun Bundle.putParcelParcelableExtra(key: String, thing: Any?) {
    this.putParcelable(key, org.parceler.Parcels.wrap(thing))
}