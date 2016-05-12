package com.commit451.gitlab.event;

import com.commit451.gitlab.model.Account;

/**
 * Called when a user logs in
 */
public class LoginEvent {
    public Account account;

    public LoginEvent(Account account) {
        this.account = account;
    }
}
