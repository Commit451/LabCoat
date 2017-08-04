package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel
open class UserFull : User() {
    @Json(name = "email")
    var email: String? = null
    @Json(name = "theme_id")
    var themeId: Int = 0
    @Json(name = "color_scheme_id")
    var colorSchemeId: Int = 0
    @Json(name = "projects_limit")
    var projectsLimit: Int = 0
    @Json(name = "current_sign_in_at")
    var currentSignInAt: Date? = null
    @Json(name = "identities")
    var identities: List<Identity>? = null
    @Json(name = "can_create_group")
    var canCreateGroup: Boolean = false
    @Json(name = "can_create_project")
    var canCreateProject: Boolean = false
    @Json(name = "two_factor_enabled")
    var isTwoFactorEnabled: Boolean = false
}
