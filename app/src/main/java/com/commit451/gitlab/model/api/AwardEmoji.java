package com.commit451.gitlab.model.api;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.parceler.Parcel;

import java.util.Date;

/**
 * http://docs.gitlab.com/ce/api/award_emoji.html
 */
@JsonObject
@Parcel
public class AwardEmoji {

    @JsonField(name = "id")
    String id;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "user")
    UserBasic user;
    @JsonField(name = "created_at")
    Date createdAt;
    @JsonField(name = "updated_at")
    Date updatedAt;
    @JsonField(name = "awardable_id")
    int awardableId;
    @JsonField(name = "awardable_type")
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
