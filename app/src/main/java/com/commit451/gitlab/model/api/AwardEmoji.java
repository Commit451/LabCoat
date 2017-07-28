package com.commit451.gitlab.model.api;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.Date;

/**
 * http://docs.gitlab.com/ce/api/award_emoji.html
 */
@Parcel
public class AwardEmoji {

    @Json(name = "id")
    String id;
    @Json(name = "name")
    String name;
    @Json(name = "user")
    UserBasic user;
    @Json(name = "created_at")
    Date createdAt;
    @Json(name = "updated_at")
    Date updatedAt;
    @Json(name = "awardable_id")
    int awardableId;
    @Json(name = "awardable_type")
    String awardableType;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UserBasic getUser() {
        return user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public int getAwardableId() {
        return awardableId;
    }

    public String getAwardableType() {
        return awardableType;
    }
}
