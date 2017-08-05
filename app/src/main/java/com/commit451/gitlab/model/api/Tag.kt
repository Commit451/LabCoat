package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

/**
 * A tag in Git
 */
class Tag {

    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "message")
    var message: String? = null
    @field:Json(name = "commit")
    var commit: Commit? = null
    @field:Json(name = "release")
    var release: Release? = null

    class Commit {

        @field:Json(name = "id")
        var id: String? = null
        @field:Json(name = "message")
        var message: String? = null
    }

    class Release {
        @field:Json(name = "tag_name")
        var tagName: String? = null
        @field:Json(name = "description")
        var description: String? = null
    }
}
