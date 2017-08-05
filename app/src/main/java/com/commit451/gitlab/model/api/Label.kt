package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

/**
 * A label
 */
@Parcel(Parcel.Serialization.BEAN)
open class Label {

    @field:Json(name = "color")
    var color: String? = null
    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "description")
    var description: String? = null
    @field:Json(name = "open_issues_count")
    var openIssuesCount: Int = 0
    @field:Json(name = "closed_issues_count")
    var closedIssuesCount: Int = 0
    @field:Json(name = "open_merge_requests_count")
    var openMergeRequestsCount: Int = 0
    @field:Json(name = "subscribed")
    var isSubscribed: Boolean = false
}
