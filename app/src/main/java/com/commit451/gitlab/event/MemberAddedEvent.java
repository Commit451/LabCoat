package com.commit451.gitlab.event;

import com.commit451.gitlab.model.api.Member;

/**
 * Indicates that a user was added
 */
public class MemberAddedEvent {
    public final Member member;

    public MemberAddedEvent(Member member) {
        this.member = member;
    }
}
