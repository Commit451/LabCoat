package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

/**
 * Represents a build
 */
@Parcel(Parcel.Serialization.BEAN)
open class Build {

    @field:Json(name = "commit")
    var commit: RepositoryCommit? = null
    @field:Json(name = "coverage")
    var coverage: String? = null
    @field:Json(name = "created_at")
    lateinit var createdAt: Date
    @field:Json(name = "artifacts_file")
    var artifactsFile: ArtifactsFile? = null
    @field:Json(name = "finished_at")
    var finishedAt: Date? = null
    @field:Json(name = "id")
    var id: Long = 0
    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "ref")
    var ref: String? = null
    @field:Json(name = "runner")
    var runner: Runner? = null
    @field:Json(name = "pipeline")
    var pipeline: Pipeline? = null
    @field:Json(name = "stage")
    var stage: String? = null
    @field:Json(name = "started_at")
    var startedAt: Date? = null
    @field:Json(name = "status")
    var status: String? = null
    @field:Json(name = "tag")
    var isTag: Boolean = false
    @field:Json(name = "user")
    var user: User? = null
}
