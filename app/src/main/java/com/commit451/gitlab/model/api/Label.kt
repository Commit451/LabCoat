package com.commit451.gitlab.model.api

import com.squareup.moshi.Json

import org.parceler.Parcel

/**
 * A label
 */
@Parcel
class Label {

    @Json(name = "color")
    var color: String? = null
    @Json(name = "name")
    var name: String? = null
    @Json(name = "description")
    var description: String? = null
    @Json(name = "open_issues_count")
    var openIssuesCount: Int = 0
    @Json(name = "closed_issues_count")
    var closedIssuesCount: Int = 0
    @Json(name = "open_merge_requests_count")
    var openMergeRequestsCount: Int = 0
    @Json(name = "subscribed")
    var isSubscribed: Boolean = false
}
