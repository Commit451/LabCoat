package com.commit451.gitlab.event;

import com.commit451.gitlab.model.api.Milestone;

/**
 * Shows that a milestone has changed (been edited)
 */
public class MilestoneChangedEvent {
    public final Milestone mMilestone;

    public MilestoneChangedEvent(Milestone milestone) {
        this.mMilestone = milestone;
    }
}
