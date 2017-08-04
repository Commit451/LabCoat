package com.commit451.gitlab.api.request

import com.squareup.moshi.Json

/**
 * Start a session
 */
class SessionRequest {

    @field:Json(name = "email")
    var email: String? = null
    @field:Json(name = "login")
    var login: String? = null
    @field:Json(name = "password")
    var password: String? = null
}
