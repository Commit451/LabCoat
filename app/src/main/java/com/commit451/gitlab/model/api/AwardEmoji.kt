package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.Date

/**
 * http://docs.gitlab.com/ce/api/award_emoji.html
 */
@Parcelize
data class AwardEmoji(

    @Json(name = "id")
    var id: String? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "user")
    var user: User? = null,
    @Json(name = "created_at")
    var createdAt: Date? = null,
    @Json(name = "updated_at")
    var updatedAt: Date? = null,
    @Json(name = "awardable_id")
    var awardableId: Int = 0,
    @Json(name = "awardable_type")
    var awardableType: String? = null
) : Parcelable
