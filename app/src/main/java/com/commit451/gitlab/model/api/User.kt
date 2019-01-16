package com.commit451.gitlab.model.api

import androidx.annotation.StringDef
import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel(Parcel.Serialization.BEAN)
open class User {

    companion object {
        const val STATE_ACTIVE = "active"
        const val STATE_BLOCKED = "blocked"

        fun getAccessLevel(accessLevel: String): Int {
            when (accessLevel.toLowerCase()) {
                "guest" -> return 10
                "reporter" -> return 20
                "developer" -> return 30
                "master" -> return 40
                "owner" -> return 50
            }

            throw IllegalStateException("No known code for this access level")
        }

        fun getAccessLevel(accessLevel: Int): String {
            when (accessLevel) {
                10 -> return "Guest"
                20 -> return "Reporter"
                30 -> return "Developer"
                40 -> return "Master"
                50 -> return "Owner"
            }

            return "Unknown"
        }
    }

    @StringDef(STATE_ACTIVE, STATE_BLOCKED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State

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
    @field:Json(name = "email")
    var email: String? = null
    @field:Json(name = "theme_id")
    var themeId: Int? = 0
    @field:Json(name = "color_scheme_id")
    var colorSchemeId: Int = 0
    @field:Json(name = "projects_limit")
    var projectsLimit: Int = 0
    @field:Json(name = "current_sign_in_at")
    var currentSignInAt: Date? = null
    @field:Json(name = "identities")
    var identities: MutableList<Identity>? = null
    @field:Json(name = "can_create_group")
    var canCreateGroup: Boolean = false
    @field:Json(name = "can_create_project")
    var canCreateProject: Boolean = false
    @field:Json(name = "two_factor_enabled")
    var isTwoFactorEnabled: Boolean = false
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
    @field:Json(name = "name")
    var name: String? = null
    @field:Json(name = "username")
    var username: String? = null
    @field:Json(name = "private_token")
    var privateToken: String? = null
    @field:Json(name = "access_level")
    var accessLevel: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as User

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
