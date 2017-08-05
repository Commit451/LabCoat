package com.commit451.gitlab.event

import com.commit451.gitlab.model.api.User

/**
 * Indicates that a user was added
 */
class MemberAddedEvent(val member: User)
