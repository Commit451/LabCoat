package com.commit451.gitlab.event

import com.commit451.gitlab.model.api.Issue

/**
 * Oh no! An Issue!
 */
class IssueCreatedEvent(val issue: Issue)
