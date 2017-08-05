package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

/**
 * http://docs.gitlab.com/ce/api/award_emoji.html
 */
@Parcel(Parcel.Serialization.BEAN)
open class AwardEmoji {

    @field:Json(name = "id")
    var id: String? = null
    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "user")
    var user: User? = null
    @field:Json(name = "created_at")
    var createdAt: Date? = null
    @field:Json(name = "updated_at")
    var updatedAt: Date? = null
    @field:Json(name = "awardable_id")
    var awardableId: Int = 0
    @field:Json(name = "awardable_type")
    var awardableType: String? = null
}
