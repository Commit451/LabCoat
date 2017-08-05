package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

@Parcel(Parcel.Serialization.BEAN)
open class RepositoryFile {
    @field:Json(name = "file_name")
    var fileName: String? = null
    @field:Json(name = "file_path")
    var filePath: String? = null
    @field:Json(name = "size")
    var size: Long = 0
    @field:Json(name = "encoding")
    var encoding: String? = null
    @field:Json(name = "content")
    lateinit var content: String
    @field:Json(name = "ref")
    var ref: String? = null
    @field:Json(name = "blob_id")
    var blobId: String? = null
    @field:Json(name = "commit_id")
    var commitId: String? = null
    @field:Json(name = "last_commit_id")
    var lastCommitId: String? = null
}
