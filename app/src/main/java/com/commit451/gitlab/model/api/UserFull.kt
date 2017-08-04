package com.commit451.gitlab.model.api

import com.squareup.moshi.Json
import org.parceler.Parcel
import java.util.*

@Parcel
open class UserFull : User() {
    @field:Json(name = "email")
    var email: String? = null
    @field:Json(name = "theme_id")
    var themeId: Int = 0
    @field:Json(name = "color_scheme_id")
    var colorSchemeId: Int = 0
    @field:Json(name = "projects_limit")
    var projectsLimit: Int = 0
    @field:Json(name = "current_sign_in_at")
    var currentSignInAt: Date? = null
    @field:Json(name = "identities")
    var identities: List<Identity>? = null
    @field:Json(name = "can_create_group")
    var canCreateGroup: Boolean = false
    @field:Json(name = "can_create_project")
    var canCreateProject: Boolean = false
    @field:Json(name = "two_factor_enabled")
    var isTwoFactorEnabled: Boolean = false
}
