package com.commit451.gitlab.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;
@Parcel
public class User {

    @SerializedName("id")
    long id;
    @SerializedName("username")
    String username;
    @SerializedName("email")
    String email;
    @SerializedName("avatar_url")
    String avatar_url;
    @SerializedName("name")
    String name;
    @SerializedName("blocked")
    boolean blocked;
    @SerializedName("created_at")
    Date created_at;
    @SerializedName("access_level")
    int access_level = -1;

    public User(){}

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() { return email; }

    public String getAvatarUrl() {
        return avatar_url;
    }

    public String getName() {
        return name;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public int getAccessLevel() {
        return access_level;
    }

    public String getAccessLevel(String[] names) {
        int temp = access_level / 10 - 1;

        if(temp >= 0 && temp < names.length)
            return names[temp];

        return "";
    }

    /**
     * Manually creates the url to the person's recent activity feed
     * @param baseUrl the base url of the server
     * @return RSS feed url
     */
    public String getFeedUrl(String baseUrl) {
        return baseUrl + "/u/" + username + ".atom";
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof User))
            return false;

        User rhs = (User) obj;

        return rhs.id == id;
    }
}
