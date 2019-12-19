package com.commit451.gitlab.model.api

import android.os.Parcelable
import androidx.annotation.StringDef
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class User(
    @Json(name = "created_at")
    var createdAt: Date? = null,
    @Json(name = "is_admin")
    var isAdmin: Boolean = false,
    @Json(name = "bio")
    var bio: String? = null,
    @Json(name = "skype")
    var skype: String? = null,
    @Json(name = "linkedin")
    var linkedin: String? = null,
    @Json(name = "twitter")
    var twitter: String? = null,
    @Json(name = "website_url")
    var websiteUrl: String? = null,
    @Json(name = "email")
    var email: String? = null,
    @Json(name = "theme_id")
    var themeId: Int? = 0,
    @Json(name = "color_scheme_id")
    var colorSchemeId: Int = 0,
    @Json(name = "projects_limit")
    var projectsLimit: Int = 0,
    @Json(name = "current_sign_in_at")
    var currentSignInAt: Date? = null,
    @Json(name = "identities")
    var identities: List<Identity>? = null,
    @Json(name = "can_create_group")
    var canCreateGroup: Boolean = false,
    @Json(name = "can_create_project")
    var canCreateProject: Boolean = false,
    @Json(name = "two_factor_enabled")
    var isTwoFactorEnabled: Boolean = false,
    @Json(name = "id")
    var id: Long = 0,
    @Json(name = "state")
    @State
    @get:State
    var state: String? = null,
    @Json(name = "avatar_url")
    var avatarUrl: String? = null,
    @Json(name = "web_url")
    var webUrl: String? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "username")
    var username: String? = null,
    @Json(name = "private_token")
    var privateToken: String? = null,
    @Json(name = "access_level")
    var accessLevel: Int = 0
) : Parcelable {

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
}
