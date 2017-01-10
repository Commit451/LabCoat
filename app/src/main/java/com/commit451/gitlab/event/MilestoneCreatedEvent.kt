package com.commit451.gitlab.event

import com.commit451.gitlab.model.api.Milestone

/**
 * Signifies that a milestone was created
 */
class MilestoneCreatedEvent(val milestone: Milestone)
