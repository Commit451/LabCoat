package com.commit451.gitlab.events;

import com.commit451.gitlab.model.Issue;

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
