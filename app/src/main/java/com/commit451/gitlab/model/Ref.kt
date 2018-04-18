package com.commit451.gitlab.model

import android.support.annotation.IntDef
import org.parceler.Parcel

/**
 * Local only model that references either a branch or a tag, and holds its type
 */
@Parcel(Parcel.Serialization.BEAN)
open class Ref {

    companion object {
        const val TYPE_BRANCH = 0
        const val TYPE_TAG = 1
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TYPE_BRANCH, TYPE_TAG)
    annotation class Type

    var type: Int = 0
    var ref: String? = null

    constructor()

    constructor(@Type type: Int, ref: String?) {
        this.type = type
        this.ref = ref
    }
}
