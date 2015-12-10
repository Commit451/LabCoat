package com.commit451.gitlab.model;

import android.net.Uri;
import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

@Parcel
public class User {
    public static final String STATE_ACTIVE = "active";
    public static final String STATE_BLOCKED = "blocked";

    @StringDef({STATE_ACTIVE, STATE_BLOCKED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    @SerializedName("id")
    long mId;
    @SerializedName("username")
    String mUsername;
    @SerializedName("email")
    String mEmail;
    @SerializedName("name")
    String mName;
    @SerializedName("state")
    String mState;
    @SerializedName("avatar_url")
    Uri mAvatarUrl;
    @SerializedName("web_url")
    Uri mWebUrl;
    @SerializedName("bio")
    String mBio;
    @SerializedName("skype")
    String mSkype;
    @SerializedName("linkedin")
    String mLinkedin;
    @SerializedName("twitter")
    String mTwitter;
    @SerializedName("website_url")
    Uri mWebsiteUrl;
    @SerializedName("theme_id")
    int mThemeId;
    @SerializedName("color_scheme_id")
    int mColorSchemeId;
    @SerializedName("is_admin")
    boolean mIsAdmin;
    @SerializedName("can_create_group")
    boolean mCanCreateGroup;
    @SerializedName("can_create_project")
    boolean mCanCreateProject;
    @SerializedName("two_factor_enabled")
    boolean mTwoFactorEnabled;
    @SerializedName("projects_limit")
    int mProjectsLimit;
    @SerializedName("created_at")
    Date mCreatedAt;

    public User() {}

    public long getId() {
        return mId;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getName() {
        return mName;
    }

    @State
    public String getState() {
        return mState;
    }

    public Uri getAvatarUrl() {
        return mAvatarUrl;
    }

    public Uri getWebUrl() {
        return mWebUrl;
    }

    public String getBio() {
        return mBio;
    }

    public String getSkype() {
        return mSkype;
    }

    public String getLinkedin() {
        return mLinkedin;
    }

    public String getTwitter() {
        return mTwitter;
    }

    public Uri getWebsiteUrl() {
        return mWebsiteUrl;
    }

    public int getThemeId() {
        return mThemeId;
    }

    public int getColorSchemeId() {
        return mColorSchemeId;
    }

    public boolean isAdmin() {
        return mIsAdmin;
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

    public int getProjectsLimit() {
        return mProjectsLimit;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public String getFeedUrl() {
        return mWebUrl.toString() + ".atom";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return mId == user.mId;

    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }
}
