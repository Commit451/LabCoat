package com.commit451.gitlab.event;

import com.commit451.gitlab.model.api.Issue;

/**
 * Event indicating that an issue has changed
 */
public class IssueChangedEvent {
    public final Issue issue;

    public IssueChangedEvent(Issue issue) {
        this.issue = issue;
    }
}
