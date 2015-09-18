package com.commit451.gitlab.events;

import com.commit451.gitlab.model.User;

/**
 * Indicates that a user was added
 * Created by Jawn on 9/17/2015.
 */
public class UserAddedEvent {

    public User user;
    public UserAddedEvent(User user) {
        this.user = user;
    }
}
