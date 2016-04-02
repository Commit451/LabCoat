package com.commit451.gitlab.event;

import com.commit451.gitlab.model.api.Build;

/**
 * A build changed
 */
public class BuildChangedEvent {
    public final Build build;

    public BuildChangedEvent(Build build) {
        this.build = build;
    }
}
