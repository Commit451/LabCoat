package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

@Parcel
public class UserFull extends User {
    @JsonField(name = "email")
    String mEmail;
    @JsonField(name = "theme_id")
    int mThemeId;
    @JsonField(name = "color_scheme_id")
    int mColorSchemeId;
    @JsonField(name = "projects_limit")
    int mProjectsLimit;
    @JsonField(name = "current_sign_in_at")
    Date mCurrentSignInAt;
    @JsonField(name = "identities")
    List<Identity> mIdentities;
    @JsonField(name = "can_create_group")
    boolean mCanCreateGroup;
    @JsonField(name = "can_create_project")
    boolean mCanCreateProject;
    @JsonField(name = "two_factor_enabled")
    boolean mTwoFactorEnabled;

    public UserFull() {}

    public String getEmail() {
        return mEmail;
    }

    public int getThemeId() {
        return mThemeId;
    }

    public int getColorSchemeId() {
        return mColorSchemeId;
    }

    public int getProjectsLimit() {
        return mProjectsLimit;
    }

    public Date getCurrentSignInAt() {
        return mCurrentSignInAt;
    }

    public List<Identity> getIdentities() {
        return mIdentities;
    }

    public boolean canCreateGroup() {
        return mCanCreateGroup;
    }

    public boolean canCreateProject() {
        return mCanCreateProject;
    }

    public boolean isTwoFactorEnabled() {
        return mTwoFactorEnabled;
    }
}
