package com.commit451.gitlab.event

import com.commit451.gitlab.model.Account

/**
 * Called when a user logs in
 */
class LoginEvent(var account: Account)
