package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel
open class User : UserBasic() {
    @field:Json(name = "created_at")
    var createdAt: Date? = null
    @field:Json(name = "is_admin")
    var isAdmin: Boolean = false
    @field:Json(name = "bio")
    var bio: String? = null
    @field:Json(name = "skype")
    var skype: String? = null
    @field:Json(name = "linkedin")
    var linkedin: String? = null
    @field:Json(name = "twitter")
    var twitter: String? = null
    @field:Json(name = "website_url")
    var websiteUrl: String? = null
}
