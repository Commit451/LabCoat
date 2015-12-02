package com.commit451.gitlab.model;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class DeleteResponse {

    long id;
    long group_id;
    long user_id;
    long group_access;
    long notification_level;
    Date created_at;
    Date updated_at;

    public DeleteResponse(){}

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getGroupId() {
        return group_id;
    }
    public void setGroupId(long group_id) {
        this.group_id = group_id;
    }

    public long getUserId() {
        return user_id;
    }
    public void setUserId(long user_id) {
        this.user_id = user_id;
    }

    public long getGroupAccess() {
        return group_access;
    }
    public void setGroupAccess(long group_access) {
        this.group_access = group_access;
    }

    public long getNotificationLevel() {
        return notification_level;
    }
    public void setNotificationLevel(long notification_level) {
        this.notification_level = notification_level;
    }

    public Date getCreatedAt() {
        return created_at;
    }
    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }
    public void setUpdatedAt(Date updated_at) {
        this.updated_at = updated_at;
    }
}