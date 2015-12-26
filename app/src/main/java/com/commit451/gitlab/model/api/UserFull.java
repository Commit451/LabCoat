package com.commit451.gitlab.model.api;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;
import java.util.List;

@Parcel
public class UserFull extends User {
    @SerializedName("email")
    String mEmail;
    @SerializedName("theme_id")
    int mThemeId;
    @SerializedName("color_scheme_id")
    int mColorSchemeId;
    @SerializedName("projects_limit")
    int mProjectsLimit;
    @SerializedName("current_sign_in_at")
    Date mCurrentSignInAt;
    @SerializedName("identities")
    List<Identity> mIdentities;
    @SerializedName("can_create_group")
    boolean mCanCreateGroup;
    @SerializedName("can_create_project")
    boolean mCanCreateProject;
    @SerializedName("two_factor_enabled")
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
