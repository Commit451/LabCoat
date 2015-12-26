package com.commit451.gitlab.event;

import com.commit451.gitlab.model.api.Issue;

/**
 * Oh no! An Issue!
 * Created by Jawn on 7/31/2015.
 */
public class IssueCreatedEvent {
    public Issue issue;

    public IssueCreatedEvent(Issue issue) {
        this.issue = issue;
    }
}
