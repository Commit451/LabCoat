package com.commit451.gitlab.model

import com.commit451.gitlab.model.api.UserFull
import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

/**
 * An account, stored locally, which references the needed info to connect to a server
 */
@Parcel
class Account : Comparable<Account> {

    @field:Json(name = "server_url")
    var serverUrl: String? = null
    @field:Json(name = "authorization_header")
    var authorizationHeader: String? = null
    @field:Json(name = "private_token")
    var privateToken: String? = null
    @field:Json(name = "trusted_certificate")
    var trustedCertificate: String? = null
    @field:Json(name = "trusted_hostname")
    var trustedHostname: String? = null
    @field:Json(name = "private_key_alias")
    var privateKeyAlias: String? = null
    @field:Json(name = "user")
    var user: UserFull? = null
    @field:Json(name = "last_used")
    var lastUsed: Date? = null

    override fun compareTo(another: Account): Int {
        return lastUsed!!.compareTo(another.lastUsed)
    }
}
