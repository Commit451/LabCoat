package com.commit451.gitlab.event

import com.commit451.gitlab.model.api.Issue

/**
 * Event indicating that an issue has changed
 */
class IssueChangedEvent(val issue: Issue)
