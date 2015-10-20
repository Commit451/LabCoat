package com.commit451.gitlab.events;

import com.commit451.gitlab.model.Issue;

/**
 * Event indicating that an issue has changed
 * Created by Jawnnypoo on 10/19/2015.
 */
public class IssueChangedEvent {

    public Issue issue;

    public IssueChangedEvent(Issue issue) {
        this.issue = issue;
    }
}
