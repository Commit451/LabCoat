package com.commit451.gitlab.model.api

import android.support.annotation.StringDef
import com.squareup.moshi.Json
import org.parceler.Parcel

@Parcel
class RepositoryTreeObject {

    companion object {

        const val TYPE_FOLDER = "tree"
        const val TYPE_REPO = "submodule"
        const val TYPE_FILE = "blob"
    }

    @StringDef(TYPE_FOLDER, TYPE_REPO, TYPE_FILE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type

    @Json(name = "id")
    var id: String? = null
    @Json(name = "name")
    var name: String? = null
    @Json(name = "type")
    @Type
    @get:Type var type: String? = null
    @Json(name = "mode")
    var mode: String? = null
}
