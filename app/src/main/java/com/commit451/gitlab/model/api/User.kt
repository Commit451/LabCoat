package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel
open class User : UserBasic() {
    @Json(name = "created_at")
    var createdAt: Date? = null
    @Json(name = "is_admin")
    var isAdmin: Boolean = false
    @Json(name = "bio")
    var bio: String? = null
    @Json(name = "skype")
    var skype: String? = null
    @Json(name = "linkedin")
    var linkedin: String? = null
    @Json(name = "twitter")
    var twitter: String? = null
    @Json(name = "website_url")
    var websiteUrl: String? = null
}
