package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Diff(
    @field:Json(name = "old_path")
    var oldPath: String? = null,
    @field:Json(name = "new_path")
    var newPath: String? = null,
    @field:Json(name = "a_mode")
    var aMode: Int = 0,
    @field:Json(name = "b_mode")
    var bMode: Int = 0,
    @field:Json(name = "diff")
    var diff: String? = null,
    @field:Json(name = "new_file")
    var isNewFile: Boolean = false,
    @field:Json(name = "renamed_file")
    var isRenamedFile: Boolean = false,
    @field:Json(name = "deleted_file")
    var isDeletedFile: Boolean = false
) : Parcelable
