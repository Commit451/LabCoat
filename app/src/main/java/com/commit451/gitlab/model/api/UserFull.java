package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

@Parcel
@JsonObject
public class UserFull extends User {
    @JsonField(name = "email")
    String email;
    @JsonField(name = "theme_id")
    int themeId;
    @JsonField(name = "color_scheme_id")
    int colorSchemeId;
    @JsonField(name = "projects_limit")
    int projectsLimit;
    @JsonField(name = "current_sign_in_at")
    Date currentSignInAt;
    @JsonField(name = "identities")
    List<Identity> identities;
    @JsonField(name = "can_create_group")
    boolean canCreateGroup;
    @JsonField(name = "can_create_project")
    boolean canCreateProject;
    @JsonField(name = "two_factor_enabled")
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
