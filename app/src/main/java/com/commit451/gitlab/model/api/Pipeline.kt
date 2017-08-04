package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

/**
 * A pipeline.
 */
@Parcel
class Pipeline {

    @field:Json(name = "user")
    var user: CommitUser? = null
    @field:Json(name = "id")
    var id: Long = 0
    @field:Json(name = "sha")
    var sha: String? = null
    @field:Json(name = "ref")
    var ref: String? = null
    @field:Json(name = "status")
    var status: String? = null
    @field:Json(name = "created_at")
    var createdAt: Date? = null
    @field:Json(name = "started_at")
    var startedAt: Date? = null
    @field:Json(name = "finished_at")
    var finishedAt: Date? = null
}
