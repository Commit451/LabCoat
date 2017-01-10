package com.commit451.gitlab.event

import com.commit451.gitlab.model.api.Milestone

/**
 * Shows that a milestone has changed (been edited)
 */
class MilestoneChangedEvent(val milestone: Milestone)
