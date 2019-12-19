package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.Date

/**
 * Represents a build
 */
@Parcelize
data class Build(
    @Json(name = "commit")
    var commit: RepositoryCommit? = null,
    @Json(name = "coverage")
    var coverage: String? = null,
    @Json(name = "created_at")
    var createdAt: Date,
    @Json(name = "artifacts_file")
    var artifactsFile: ArtifactsFile? = null,
    @Json(name = "finished_at")
    var finishedAt: Date? = null,
    @Json(name = "id")
    var id: Long = 0,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "ref")
    var ref: String? = null,
    @Json(name = "runner")
    var runner: Runner? = null,
    @Json(name = "pipeline")
    var pipeline: Pipeline? = null,
    @Json(name = "stage")
    var stage: String? = null,
    @Json(name = "started_at")
    var startedAt: Date? = null,
    @Json(name = "status")
    var status: String? = null,
    @Json(name = "tag")
    var isTag: Boolean = false,
    @Json(name = "user")
    var user: User? = null
) : Parcelable
