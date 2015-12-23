package com.commit451.gitlab.event;

import com.commit451.gitlab.model.Account;

/**
 * Called when a user logs in
 * Created by John on 12/23/15.
 */
public class LoginEvent {
    public Account account;

    public LoginEvent(Account account) {
        this.account = account;
    }
}
