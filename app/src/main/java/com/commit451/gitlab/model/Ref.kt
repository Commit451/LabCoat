package com.commit451.gitlab.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Local only model that references either a branch or a tag, and holds its type
 */
@Parcelize
data class Ref(
    val type: Int = 0,
    val ref: String? = null
) : Parcelable {

    companion object {
        const val TYPE_BRANCH = 0
        const val TYPE_TAG = 1
    }
}
