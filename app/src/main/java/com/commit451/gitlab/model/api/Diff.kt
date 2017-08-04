package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel
class Diff {
    @Json(name = "old_path")
    var oldPath: String? = null
    @Json(name = "new_path")
    var newPath: String? = null
    @Json(name = "a_mode")
    var aMode: Int = 0
    @Json(name = "b_mode")
    var bMode: Int = 0
    @Json(name = "diff")
    var diff: String? = null
    @Json(name = "new_file")
    var isNewFile: Boolean = false
    @Json(name = "renamed_file")
    var isRenamedFile: Boolean = false
    @Json(name = "deleted_file")
    var isDeletedFile: Boolean = false
}
