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
    String mId;
    @JsonField(name = "name")
    String mName;
    @JsonField(name = "user")
    UserBasic mUser;
    @JsonField(name = "created_at")
    Date mCreatedAt;
    @JsonField(name = "updated_at")
    Date mUpdatedAt;
    @JsonField(name = "awardable_id")
    int mAwardableId;
    @JsonField(name = "awardable_type")
    String mAwardableType;

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public UserBasic getUser() {
        return mUser;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public Date getUpdatedAt() {
        return mUpdatedAt;
    }

    public int getAwardableId() {
        return mAwardableId;
    }

    public String getAwardableType() {
        return mAwardableType;
    }
}
