package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

/**
 * A pipeline.
 */
@Parcel
class Pipeline {

    @Json(name = "user")
    var user: CommitUser? = null
    @Json(name = "id")
    var id: Long = 0
    @Json(name = "sha")
    var sha: String? = null
    @Json(name = "ref")
    var ref: String? = null
    @Json(name = "status")
    var status: String? = null
    @Json(name = "created_at")
    var createdAt: Date? = null
    @Json(name = "started_at")
    var startedAt: Date? = null
    @Json(name = "finished_at")
    var finishedAt: Date? = null
}
