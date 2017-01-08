package com.commit451.gitlab.event;

import com.commit451.gitlab.model.api.Milestone;

/**
 * Signifies that a milestone was created
 */
public class MilestoneCreatedEvent {
    public final Milestone milestone;

    public MilestoneCreatedEvent(Milestone milestone) {
        this.milestone = milestone;
    }
}
