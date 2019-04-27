package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RepositoryFile(
    @field:Json(name = "file_name")
    var fileName: String? = null,
    @field:Json(name = "file_path")
    var filePath: String? = null,
    @field:Json(name = "size")
    var size: Long = 0,
    @field:Json(name = "encoding")
    var encoding: String? = null,
    @field:Json(name = "content")
    var content: String,
    @field:Json(name = "ref")
    var ref: String? = null,
    @field:Json(name = "blob_id")
    var blobId: String? = null,
    @field:Json(name = "commit_id")
    var commitId: String? = null,
    @field:Json(name = "last_commit_id")
    var lastCommitId: String? = null
) : Parcelable
