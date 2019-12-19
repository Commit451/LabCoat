package com.commit451.gitlab.model.api

import android.os.Parcelable
import androidx.annotation.StringDef
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RepositoryTreeObject(
    @Json(name = "id")
    var id: String? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "type")
    @Type
    @get:Type
    var type: String? = null,
    @Json(name = "mode")
    var mode: String? = null
) : Parcelable {
    companion object {

        const val TYPE_FOLDER = "tree"
        const val TYPE_REPO = "submodule"
        const val TYPE_FILE = "blob"
    }

    @StringDef(TYPE_FOLDER, TYPE_REPO, TYPE_FILE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type
}
