package com.commit451.gitlab.api.request

import com.squareup.moshi.Json

/**
 * Start a session
 */
class SessionRequest {

    @Json(name = "email")
    var email: String? = null
    @Json(name = "login")
    var login: String? = null
    @Json(name = "password")
    var password: String? = null
}
