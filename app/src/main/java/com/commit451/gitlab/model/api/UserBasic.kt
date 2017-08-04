package com.commit451.gitlab.model.api

import android.support.annotation.StringDef
import com.squareup.moshi.Json
import org.parceler.Parcel


@Parcel
open class UserBasic : UserSafe() {

    companion object {

        const val STATE_ACTIVE = "active"
        const val STATE_BLOCKED = "blocked"
    }

    @StringDef(STATE_ACTIVE, STATE_BLOCKED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State

    @field:Json(name = "id")
    var id: Long = 0
    @field:Json(name = "state")
    @State
    @get:State
    var state: String? = null
    @field:Json(name = "avatar_url")
    var avatarUrl: String? = null
    @field:Json(name = "web_url")
    lateinit var webUrl: String
}
