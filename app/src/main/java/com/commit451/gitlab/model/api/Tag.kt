package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

/**
 * A tag in Git
 */
data class Tag(
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "message")
    var message: String? = null,
    @Json(name = "commit")
    var commit: Commit? = null,
    @Json(name = "release")
    var release: Release? = null
) {


    data class Commit(
        @Json(name = "id")
        var id: String? = null,
        @Json(name = "message")
        var message: String? = null
    )

    data class Release(
        @Json(name = "tag_name")
        var tagName: String? = null,
        @Json(name = "description")
        var description: String? = null
    )
}
