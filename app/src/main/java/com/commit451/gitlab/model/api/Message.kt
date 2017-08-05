package com.commit451.gitlab.model.api


import com.squareup.moshi.Json

/**
 * The structure for a message from the server, which is usually found in a response body
 */
class Message {

    @field:Json(name = "message")
    var message: String? = null
}
