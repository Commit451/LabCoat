package com.commit451.gitlab.model

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

/**
 * An account, stored locally, which references the needed info to connect to a server
 */
@Parcel(Parcel.Serialization.BEAN)
open class Account : Comparable<Account> {

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
    @field:Json(name = "email")
    var email: String? = null
    @field:Json(name = "username")
    var username: String? = null
    @field:Json(name = "last_used")
    var lastUsed: Date? = null

    override fun compareTo(other: Account): Int {
        return lastUsed?.compareTo(other.lastUsed) ?: 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (serverUrl != other.serverUrl) return false
        if (privateToken != other.privateToken) return false

        return true
    }

    override fun hashCode(): Int {
        var result = serverUrl?.hashCode() ?: 0
        result = 31 * result + (privateToken?.hashCode() ?: 0)
        return result
    }
}
