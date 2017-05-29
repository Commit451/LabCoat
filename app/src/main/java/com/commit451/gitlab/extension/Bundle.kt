package com.commit451.gitlab.extension

import android.os.Bundle
import org.parceler.Parcels

//Bundle extensions

fun Bundle.putParcelParcelableExtra(key: String, thing: Any?) {
    this.putParcelable(key, org.parceler.Parcels.wrap(thing))
}

/**
 * Get Parcelable that was put in the bundle using Parceler
 */
fun <T> Bundle.getParcelerParcelable(key: String): T? {
    return Parcels.unwrap<T>(getParcelable(key))
}