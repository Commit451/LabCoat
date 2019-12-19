package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RepositoryFile(
    @Json(name = "file_name")
    var fileName: String? = null,
    @Json(name = "file_path")
    var filePath: String? = null,
    @Json(name = "size")
    var size: Long = 0,
    @Json(name = "encoding")
    var encoding: String? = null,
    @Json(name = "content")
    var content: String,
    @Json(name = "ref")
    var ref: String? = null,
    @Json(name = "blob_id")
    var blobId: String? = null,
    @Json(name = "commit_id")
    var commitId: String? = null,
    @Json(name = "last_commit_id")
    var lastCommitId: String? = null
) : Parcelable
