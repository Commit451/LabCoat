package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

@Parcel
public class UserFull extends User {
    @Json(name = "email")
    String email;
    @Json(name = "theme_id")
    int themeId;
    @Json(name = "color_scheme_id")
    int colorSchemeId;
    @Json(name = "projects_limit")
    int projectsLimit;
    @Json(name = "current_sign_in_at")
    Date currentSignInAt;
    @Json(name = "identities")
    List<Identity> identities;
    @Json(name = "can_create_group")
    boolean canCreateGroup;
    @Json(name = "can_create_project")
    boolean canCreateProject;
    @Json(name = "two_factor_enabled")
    boolean twoFactorEnabled;

    public UserFull() {}

    public String getEmail() {
        return email;
    }

    public int getThemeId() {
        return themeId;
    }

    public int getColorSchemeId() {
        return colorSchemeId;
    }

    public int getProjectsLimit() {
        return projectsLimit;
    }

    public Date getCurrentSignInAt() {
        return currentSignInAt;
    }

    public List<Identity> getIdentities() {
        return identities;
    }

    public boolean canCreateGroup() {
        return canCreateGroup;
    }

    public boolean canCreateProject() {
        return canCreateProject;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }
}
