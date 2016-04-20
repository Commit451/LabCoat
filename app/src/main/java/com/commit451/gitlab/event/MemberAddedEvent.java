package com.commit451.gitlab.event;

import com.commit451.gitlab.model.api.Member;

/**
 * Indicates that a user was added
 */
public class MemberAddedEvent {
    public final Member mMember;

    public MemberAddedEvent(Member member) {
        this.mMember = member;
    }
}
